package ch.vorburger.stereotype;

/**
 * Stereotype for components with lifecycle (init/close).
 *
 * <p>Originally née as <a href="
 * https://github.com/opendaylight/infrautils/blob/ce8ac9a033e4c6d0d2e378415178fda0f5c83096/inject/inject/src/main/java/org/opendaylight/infrautils/inject/Lifecycle.java
 * ">org.opendaylight.infrautils.inject.Lifecycle</a>.
 *
 * @author Michael Vorburger
 */
public interface Lifecycled extends AutoCloseable {

    default void init() throws Exception {}

    @Override
    default void close() throws Exception {}
}
