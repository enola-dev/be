package dev.enola.common.function;

/**
 * {@link java.util.function.Consumer}-like functional interface which can throw a checked
 * exception.
 */
@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception> {

    // From
    // https://github.com/enola-dev/enola/blob/main/java/dev/enola/common/function/CheckedConsumer.java
    // which took it from
    // https://javadocs.opendaylight.org/infrautils/neon/org/opendaylight/infrautils/utils/function/package-summary.html
    // Both were originally written by Michael Vorburger.ch

    void accept(T input) throws E;
}
