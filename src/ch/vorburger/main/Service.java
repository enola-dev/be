package ch.vorburger.main;

public interface Service<I, O> extends AutoCloseable {

    default void start() throws Exception {}

    O invoke(I input) throws Exception;

    @Override
    default void close() throws Exception {}
}
