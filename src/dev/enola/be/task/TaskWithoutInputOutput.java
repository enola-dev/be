package dev.enola.be.task;

public abstract class TaskWithoutInputOutput extends Task<Empty, Empty> {

    protected TaskWithoutInputOutput() {
        super(Empty.INSTANCE);
    }

    @Override
    protected final Empty execute() throws Exception {
        executeIt();
        return Empty.INSTANCE;
    }

    protected abstract void executeIt() throws Exception;
}
