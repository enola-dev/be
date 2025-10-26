package ch.vorburger.jvmtools;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class Classpath {

    public static Classpath from(Set<URL> urls) {
        return from(new URLClassLoader(urls.toArray(new URL[urls.size()])));
    }

    public static Classpath from(ClassLoader classLoader) {
        return new ClassLoaderClasspath(classLoader);
    }

    abstract URL get(String resourcePath);

    public void setOutputDirectory(String string) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'setOutputDirectory'");
    }

    private static class ClassLoaderClasspath extends Classpath {
        private final ClassLoader classLoader;
        private final List<URL> classpath;

        ClassLoaderClasspath(ClassLoader classLoader) {
            this.classLoader = classLoader;
            this.classpath = getClasspath(classLoader);
        }

        @Override
        URL get(String resourcePath) {
            var stream = classLoader.resources(resourcePath);
            var urls = stream.toList();
            var size = urls.size();
            if (size == 0) {
                throw new IllegalArgumentException(resourcePath + " not found:" + classpath);
            } else if (size > 1) {
                throw new IllegalArgumentException(resourcePath + " found multiple times:" + urls);
            }
            return urls.get(0);
        }
    }

    private static List<URL> getClasspath(ClassLoader loader) {
        var result = new ArrayList<URL>();
        while (loader != null) {
            collectClasspath(loader, result);
            loader = loader.getParent();
        }
        return result;
    }

    private static void collectClasspath(ClassLoader loader, List<URL> result) {
        String loaderClassName = loader.getClass().getName();
        if (loader instanceof java.net.URLClassLoader ucl) {
            var urls = Arrays.asList(ucl.getURLs());
            result.addAll(urls);
            // TODO } else if (loader instanceof jdk.internal.loader.BuiltinClassLoader bcl) {
        } else if ("jdk.internal.loader.ClassLoaders$AppClassLoader".equals(loaderClassName)) {
            // Java 9+
            var sysClasspath = System.getProperty("java.class.path");
            var pathElements = sysClasspath.split(System.getProperty("path.separator"));
            for (var pathElement : pathElements) {
                Path path = Path.of(pathElement);
                result.add(Utils.toURL(path.toUri()));
            }
        } else if ("jdk.internal.loader.ClassLoaders$PlatformClassLoader".equals(loaderClassName)) {
            // SKIP (until TODO above is implemented...)
        } else
            throw new IllegalArgumentException(
                    "TODO https://www.baeldung.com/java-classloader-get-classpath: "
                            + loader.toString());
    }
}
