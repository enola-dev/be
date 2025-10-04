package dev.enola.be.io;

import dev.enola.be.task.Status;
import dev.enola.be.task.TaskWithoutInputOutput;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class NonBlockingLineWriter extends TaskWithoutInputOutput implements LineWriter {

    // TODO close() method to flush remaining messages? How would TaskExecutor know when to call it?

    private final LineWriter delegate;
    private final BlockingQueue<Object> queue;
    private boolean overflow;

    public NonBlockingLineWriter(int queueCapacity, LineWriter delegate) {
        this.delegate = delegate;
        this.queue = new ArrayBlockingQueue<>(queueCapacity, false);
    }

    @Override
    public void println(Object line) throws IOException {
        if (status() != Status.IN_PROGRESS) throw new IllegalStateException();
        if (!queue.offer(line)) overflow = true;
    }

    @Override
    protected void executeIt() throws Exception {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (overflow) {
                    // https://en.wikipedia.org/wiki/Ellipsis
                    delegate.println("[… output ... ※ ... truncated …]");
                    overflow = false;
                }

                // take() will block and wait efficiently until a message is available.
                Object line = queue.take();
                delegate.println(line);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
