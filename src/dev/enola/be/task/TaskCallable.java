package dev.enola.be.task;

import java.util.concurrent.Callable;

class TaskCallable<T> implements Callable<T> {

    private final Task<?, T> task;

    TaskCallable(Task<?, T> task) {
        this.task = task;
    }

    @Override
    public T call() throws Exception {
        var thread = Thread.currentThread();
        var originalThreadName = thread.getName();
        thread.setName(task.id().toString());
        try {
            return task.execute();
        } finally {
            thread.setName(originalThreadName);
        }
    }
}
