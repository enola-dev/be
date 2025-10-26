package dev.enola.be.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
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
public record FileSet(Set<Path> roots, List<PathMatcher> includes, List<PathMatcher> excludes) {

    // https://github.com/enola-dev/enola/tree/main/java/dev/enola/common/io/resource/stream

    // TODO boolean allowEmpty (like in Bazel)

    // TODO boolean followSymLinks (like in Ant)

    // TODO Add default excludes of common VCS dirs like .git, .svn, .hg, etc.

    public Builder builder() {
        return new Builder().merge(this);
    }

    public static class Builder {
        private final Set<Path> roots = new java.util.HashSet<>();
        private final List<PathMatcher> includes = new java.util.ArrayList<>();
        private final List<PathMatcher> excludes = new java.util.ArrayList<>();

        public Builder addRoot(Path root) {
            this.roots.add(root);
            return this;
        }

        public Builder addRoot(String root) {
            this.roots.add(Path.of(root));
            return this;
        }

        public Builder includeGlob(String pattern) {
            this.includes.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern));
            return this;
        }

        public Builder includeRegEx(String pattern) {
            this.includes.add(FileSystems.getDefault().getPathMatcher("regex:" + pattern));
            return this;
        }

        public Builder excludeGlob(String pattern) {
            this.excludes.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern));
            return this;
        }

        public Builder excludeRegEx(String pattern) {
            this.excludes.add(FileSystems.getDefault().getPathMatcher("regex:" + pattern));
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
            return new FileSet(Set.copyOf(roots), includes, excludes);
        }
    }

    public Stream<Path> stream() {
        // TODO
        // https://github.com/enola-dev/enola/blob/main/java/dev/enola/common/function/MoreStreams.java ?
        return roots.stream()
                .flatMap(
                        root -> {
                            try {
                                return Files.walk(root)
                                        .filter(Files::isRegularFile)
                                        .filter(
                                                path -> {
                                                    var relativePath = root.relativize(path);

                                                    if (!includes.isEmpty()) {
                                                        var included = false;
                                                        for (var include : includes) {
                                                            if (include.matches(relativePath)) {
                                                                included = true;
                                                                break;
                                                            }
                                                        }
                                                        if (!included) return false;
                                                    }

                                                    for (var exclude : excludes) {
                                                        if (exclude.matches(relativePath)) {
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
