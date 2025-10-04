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
            var output = task.execute();
            if (output == null)
                throw new NullPointerException("Task.execute() must not return null: " + task.id());
            return output;

        } finally {
            thread.setName(originalThreadName);
        }
    }
}
