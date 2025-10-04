package dev.enola.be.task;

import dev.enola.be.task.test.FailingTask;
import dev.enola.be.task.test.ImmediateTask;
import dev.enola.be.task.test.SlowTask;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CancellationException;

public class TaskExecutorTest {

    public static void main(String[] args) throws Exception {
        testCompletedTask();
        testFailingTasks();
        testTimingOutTask();
        testCancelTask();
        testGetTask();
        testGetNonExistentTask();
        testListTasks();
        testTaskStatusProgression();
        testResubmitTaskFailure();
        testSubmitTaskToAnotherExecutorFailure();
        testExecutorClose();
        testThreadNaming();
    }

    private static void testCompletedTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new ImmediateTask("test");
            var result = executor.await(task);
            var output = task.output().get();
            assert result == output : "The result and output objects must be the same";
            assert "Result: test".equals(result) : "Result should match expected output";

            var toString = task.toString();
            assert toString.contains("ImmediateTask") : "toString should contain class name";
            assert toString.contains("id: " + task.id().toString()) : "toString should contain ID";
            assert toString.contains("input: test") : "toString should contain input";
            assert toString.contains("output: Result: test") : "toString should contain output";
            assert toString.contains("status: COMPLETED") : "toString should contain COMPLETED";
        }
    }

    private static void testFailingTasks() throws Exception {
        for (FailingTask.FailureMode mode : FailingTask.FailureMode.values()) {
            testFailingTask(mode);
        }
    }

    private static void testFailingTask(FailingTask.FailureMode failureMode) throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new FailingTask(failureMode);

            try {
                executor.await(task);
                assert false : "Should have thrown an exception";
            } catch (Throwable e) {
                // Expected
            }

            assert task.status() == Status.FAILED : "Status should be FAILED after exception";

            var toString = task.toString();
            assert !toString.contains("output") : "toString should not contain output";
            assert toString.contains("status: FAILED") : "toString should contain FAILED";
            assert toString.contains("failure:") : "toString should contain failure:";
            assert toString.contains("exception") : "toString should contain exception";
        }
    }

    private static void testTimingOutTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new SlowTask("test", 1000, Duration.ofMillis(1));
            try {
                executor.await(task);
                assert false : "Should have thrown an exception due to timeout";

            } catch (UncheckedTaskAwaitException e) {
                assert e.getCause() instanceof CancellationException
                        : "Cause should be CancellationException";
                // Expected
            }
            assert task.status() == Status.CANCELLED
                    : "Status should now be CANCELLED, but is " + task.status();

            var toString = task.toString();
            assert !toString.contains("output") : "toString should not contain output";
            assert toString.contains("status: CANCELLED") : "toString should contain CANCELLED";
        }
    }

    private static void testCancelTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new SlowTask("test", 1000);
            executor.async(task);

            task.cancel();

            try {
                task.await();
                assert false : "Should have thrown CancellationException";
            } catch (UncheckedTaskAwaitException e) {
                // Expected
            }

            assert task.status() == Status.CANCELLED
                    : "Status should be CANCELLED after cancel(), but is " + task.status();
        }
    }

    private static void testGetTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new ImmediateTask("test");
            var taskId = task.id();
            executor.async(task);

            var retrieved = executor.get(taskId);
            assert retrieved.id().equals(taskId) : "Retrieved task ID should match";
        }
    }

    private static void testGetNonExistentTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var randomId = UUID.randomUUID();
            try {
                executor.get(randomId);
                assert false : "Should have thrown IllegalArgumentException";
            } catch (IllegalArgumentException e) {
                assert e.getMessage().contains("No such task")
                        : "Error message should mention 'No such task'";
            }
        }
    }

    private static void testListTasks() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task1 = new ImmediateTask("test1");
            var task2 = new ImmediateTask("test2");

            executor.async(task1);
            executor.async(task2);

            var taskIds = executor.list();
            assert taskIds.size() == 2 : "Should have 2 tasks, got " + taskIds.size();
            assert taskIds.contains(task1.id()) : "Should contain task1 ID";
            assert taskIds.contains(task2.id()) : "Should contain task2 ID";
        }
    }

    private static void testTaskStatusProgression() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new SlowTask("test", 1000);

            assert task.status() == Status.PENDING : "Status should be PENDING: " + task.status();
            assert task.toString().contains("status: PENDING") : "toString !PENDING";

            executor.async(task);

            var status = task.status();
            assert status == Status.IN_PROGRESS : "Status should be IN_PROGRESS, got " + status;
            assert task.toString().contains("status: IN_PROGRESS") : "toString !IN_PROGRESS";

            task.await();

            assert task.status() == Status.COMPLETED : "Final status should be COMPLETED";
            assert task.toString().contains("status: COMPLETED") : "toString !COMPLETED";
        }
    }

    private static void testResubmitTaskFailure() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new ImmediateTask("test");
            executor.async(task);

            try {
                executor.async(task);
                assert false : "Should have thrown IllegalStateException on resubmit";
            } catch (IllegalStateException e) {
                assert e.getMessage().contains("already submitted")
                        : "Error message should mention 'already submitted'";
            }
        }
    }

    private static void testSubmitTaskToAnotherExecutorFailure() throws Exception {
        var task = new ImmediateTask("test");
        try (var executor1 = new TaskExecutor()) {
            executor1.async(task);
        }
        try (var executor2 = new TaskExecutor()) {
            try {
                executor2.async(task);
                assert false : "Should have thrown IllegalStateException on resubmit";
            } catch (IllegalStateException e) {
                assert e.getMessage().contains("not PENDING")
                        : "Error message should mention 'not PENDING'";
            }
        }
    }

    private static void testExecutorClose() throws Exception {
        var executor = new TaskExecutor();
        var task = new ImmediateTask("test");
        executor.async(task);
        executor.close();
    }

    private static void testThreadNaming() {
        try (var executor = new TaskExecutor()) {
            var task =
                    new Task<Empty, String>(Empty.INSTANCE) {
                        @Override
                        protected String execute() {
                            return Thread.currentThread().getName();
                        }
                    };
            var threadName = executor.await(task);
            assert threadName.equals(task.id().toString());
        }
    }
}
