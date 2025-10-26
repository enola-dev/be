package ch.vorburger.stereotype;

public interface Service<I, O> { // NOT extends Lifecycled, as that's orthogonal.

    O invoke(I input) throws Exception;
}
