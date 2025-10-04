package dev.enola.be.task;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

class TaskCallable<T> implements Callable<T> {

    private static final Logger LOG = Logger.getLogger(TaskCallable.class.getName());

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
            task.endedAt(Instant.now());

            // TODO This will always be status: IN_PROGRESS, because we are still in the task
            // execution when we are here.  We need a hook AFTER the task has fully completed, to
            // log that final status.

            // TODO Why does LOG.fine() not work in LongIncrementingTask ?!
            LOG.fine(() -> task.toString());

            // TODO Eventually remove this System.out, once LOG.fine() works
            System.out.println(task.toString());
        }
    }
}
