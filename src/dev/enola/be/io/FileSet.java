package dev.enola.be.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

// TODO Use Guava ImmutableSet etc.
/**
 * FileSet represents a set of file paths defined by roots, with includes and excludes glob
 * patterns. Directories are traversed recursively. Directories themselves are not included in the
 * result.
 *
 * <p>Inspired by <a href="https://ant.apache.org/manual/Types/fileset.html">Apache Ant's
 * FileSet</a>, Gradle's <a
 * href="https://docs.gradle.org/current/userguide/working_with_files.html#sec:file_collections">
 * FileCollection &amp; FileTree</a>, Bazel's <a
 * href="https://bazel.build/versions/8.4.0/reference/be/functions#glob">glob() function</a> and <a
 * href="
 * https://github.com/enola-dev/enola/blob/main/java/dev/enola/common/io/resource/stream/FileGlobResolver.java
 * ">Enola.dev's FileGlobResolver</a>.
 */
public record FileSet(Set<Path> roots, List<String> includes, List<String> excludes) {

    // https://github.com/enola-dev/enola/tree/main/java/dev/enola/common/io/resource/stream

    // TODO boolean allowEmpty (like in Bazel)

    // TODO boolean followSymLinks (like in Ant)

    // TODO Add default excludes of common VCS dirs like .git, .svn, .hg, etc.

    public Builder builder() {
        return new Builder().merge(this);
    }

    public static class Builder {
        private final Set<Path> roots = new java.util.HashSet<>();
        private final List<String> includes = new java.util.ArrayList<>();
        private final List<String> excludes = new java.util.ArrayList<>();

        public Builder addRoot(Path root) {
            this.roots.add(root);
            return this;
        }

        public Builder addInclude(String pattern) {
            this.includes.add(pattern);
            return this;
        }

        public Builder addExclude(String pattern) {
            this.excludes.add(pattern);
            return this;
        }

        public Builder merge(FileSet fileSet) {
            this.roots.addAll(fileSet.roots());
            this.includes.addAll(fileSet.includes());
            this.excludes.addAll(fileSet.excludes());
            return this;
        }

        public Builder merge(Builder fileSetBuilder) {
            this.roots.addAll(fileSetBuilder.roots);
            this.includes.addAll(fileSetBuilder.includes);
            this.excludes.addAll(fileSetBuilder.excludes);
            return this;
        }

        public FileSet build() {
            return new FileSet(Set.copyOf(roots), List.copyOf(includes), List.copyOf(excludes));
        }
    }

    public Stream<Path> stream() {
        var includeMatchers =
                includes.stream()
                        .map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern))
                        .toList();
        var excludeMatchers =
                excludes.stream()
                        .map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern))
                        .toList();

        return roots.stream()
                .flatMap(
                        root -> {
                            try {
                                return Files.walk(root)
                                        .filter(Files::isRegularFile)
                                        .filter(
                                                path -> {
                                                    var relativePath = root.relativize(path);

                                                    if (!includeMatchers.isEmpty()) {
                                                        var included = false;
                                                        for (var matcher : includeMatchers) {
                                                            if (matcher.matches(relativePath)) {
                                                                included = true;
                                                                break;
                                                            }
                                                        }
                                                        if (!included) return false;
                                                    }

                                                    for (var matcher : excludeMatchers) {
                                                        if (matcher.matches(relativePath)) {
                                                            return false;
                                                        }
                                                    }
                                                    return true;
                                                });
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
    }
}
