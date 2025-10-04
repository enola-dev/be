package dev.enola.be.task;

import dev.enola.be.task.test.FailingTask;
import dev.enola.be.task.test.ImmediateTask;
import dev.enola.be.task.test.SlowTask;

import java.time.Duration;
import java.util.UUID;

public class TaskExecutorTest {

    public static void main(String[] args) throws Exception {
        testCompletedTask();
        testFailingTask();
        testTimingOutTask();
        testCancelTask();
        testGetTask();
        testGetNonExistentTask();
        testListTasks();
        testTaskStatusProgression();
        testResubmitTaskFailure();
        testSubmitTaskToAnotherExecutorFailure();
        testExecutorClose();
    }

    private static void testCompletedTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new ImmediateTask("test");
            var future = executor.future(task);
            var result = future.get();
            assert "Result: test".equals(result) : "Result should match expected output";
        }
    }

    private static void testFailingTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new FailingTask("test");
            var future = executor.future(task);

            try {
                future.get();
                assert false : "Should have thrown an exception";
            } catch (Exception e) {
                // Expected
            }

            Thread.sleep(100);

            assert task.status() == Status.FAILED : "Status should be FAILED after exception";
        }
    }

    private static void testTimingOutTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new SlowTask("test", 1000, Duration.ofMillis(1));
            executor.submit(task);
            Thread.sleep(100);
            assert task.status() == Status.CANCELLED
                    : "Status should now be CANCELLED, but is " + task.status();
        }
    }

    private static void testCancelTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new SlowTask("test", 1000);
            var future = executor.future(task);

            Thread.sleep(100);

            task.cancel();

            Thread.sleep(100);

            assert task.status() == Status.CANCELLED
                    : "Status should be CANCELLED after cancel(), but is " + task.status();
            assert future.isCancelled() : "Future should be cancelled";
        }
    }

    private static void testGetTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new ImmediateTask("test");
            var taskId = task.id();
            executor.future(task);

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

            executor.future(task1);
            executor.future(task2);

            var taskIds = executor.list();
            assert taskIds.size() == 2 : "Should have 2 tasks, got " + taskIds.size();
            assert taskIds.contains(task1.id()) : "Should contain task1 ID";
            assert taskIds.contains(task2.id()) : "Should contain task2 ID";
        }
    }

    private static void testTaskStatusProgression() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new SlowTask("test", 1000);

            assert task.status() == Status.PENDING : "Initial status should be PENDING";

            var future = executor.future(task);

            Thread.sleep(100);

            var status = task.status();
            assert status == Status.IN_PROGRESS || status == Status.COMPLETED
                    : "Status should be IN_PROGRESS or SUCCESSFUL, got " + status;

            future.get();

            assert task.status() == Status.COMPLETED : "Final status should be SUCCESSFUL";
        }
    }

    private static void testResubmitTaskFailure() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new ImmediateTask("test");
            executor.future(task);

            try {
                executor.future(task);
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
            executor1.future(task);
        }
        try (var executor2 = new TaskExecutor()) {
            try {
                executor2.future(task);
                assert false : "Should have thrown IllegalStateException on resubmit";
            } catch (IllegalStateException e) {
                assert e.getMessage().contains("not PENDING")
                        : "Error message should mention 'not PENDING:'";
            }
        }
    }

    private static void testExecutorClose() throws Exception {
        var executor = new TaskExecutor();
        var task = new ImmediateTask("test");
        executor.future(task);

        executor.close();
    }
}
