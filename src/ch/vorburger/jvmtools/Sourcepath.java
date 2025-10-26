package ch.vorburger.jvmtools;

import static ch.vorburger.jvmtools.Utils.toURI;
import static ch.vorburger.jvmtools.Utils.toURL;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

public class Sourcepath {

    // NB: For now, the Charset is hardcoded to UTF-8; could be made configurable later, if needed.

    private final List<JavaFileObject> javaFileObjects = new java.util.ArrayList<>();

    public void addPath(Path path) {
        javaFileObjects.add(new UrlJavaFileObject(toURL(path.toUri())));
    }

    public void addClasspathResource(String path) {
        addClasspathResource(Thread.currentThread().getContextClassLoader(), path);
    }

    public void addClasspathResource(ClassLoader classLoader, String path) {
        Classpath cp = Classpath.from(classLoader);
        javaFileObjects.add(new UrlJavaFileObject(cp.get(path)));
    }

    Iterable<? extends JavaFileObject> getJavaFileObjects() {
        return javaFileObjects;
    }

    private static class UrlJavaFileObject extends SimpleJavaFileObject {
        private final URL url;

        private UrlJavaFileObject(URL url) {
            super(toURI(url), Kind.SOURCE);
            this.url = url;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            try (var stream = url.openStream()) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read content from URL: " + url, e);
            }
        }
    }
}
