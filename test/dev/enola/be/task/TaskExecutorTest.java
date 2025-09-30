package dev.enola.be.task;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

public class TaskExecutorTest {

    static class SimpleTask extends Task<String, String> {
        protected SimpleTask(String input) {
            super(input);
        }

        @Override
        protected String execute() throws Exception {
            return "Result: " + input;
        }
    }
    
    static class FailingTask extends Task<String, String> {
        protected FailingTask(String input) {
            super(input);
        }

        @Override
        protected String execute() throws Exception {
            throw new RuntimeException("Intentional failure");
        }
    }
    
    static class SlowTask extends Task<String, String> {
        protected SlowTask(String input) {
            super(input);
        }

        @Override
        protected String execute() throws Exception {
            Thread.sleep(1000); // Sleep for 1 second
            return "Completed: " + input;
        }
    }

    public static void main(String[] args) throws Exception {
        testSubmitTask();
        testGetTask();
        testGetNonExistentTask();
        testListTasks();
        testTaskStatusProgression();
        testFailingTask();
        testCancelTask();
        testExecutorClose();
    }
    
    private static void testSubmitTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new SimpleTask("test");
            var future = executor.submit(task);
            
            assert future != null : "Future should not be null";
            var result = future.get();
            assert result.equals("Result: test") : "Result should match expected output";
        }
    }
    
    private static void testGetTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new SimpleTask("test");
            var taskId = task.id();
            executor.submit(task);
            
            var retrieved = executor.get(taskId);
            assert retrieved != null : "Retrieved task should not be null";
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
                assert e.getMessage().contains("No such task") : "Error message should mention 'No such task'";
            }
        }
    }
    
    private static void testListTasks() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task1 = new SimpleTask("test1");
            var task2 = new SimpleTask("test2");
            
            executor.submit(task1);
            executor.submit(task2);
            
            var taskIds = executor.list();
            assert taskIds.size() == 2 : "Should have 2 tasks, got " + taskIds.size();
            assert taskIds.contains(task1.id()) : "Should contain task1 ID";
            assert taskIds.contains(task2.id()) : "Should contain task2 ID";
        }
    }
    
    private static void testTaskStatusProgression() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new SlowTask("test");
            
            // Initial status should be PENDING
            assert task.status() == Status.PENDING : "Initial status should be PENDING";
            
            var future = executor.submit(task);
            
            // Give it a moment to start
            Thread.sleep(100);
            
            // Should be IN_PROGRESS or already done
            var status = task.status();
            assert status == Status.IN_PROGRESS || status == Status.SUCCESSFUL : 
                "Status should be IN_PROGRESS or SUCCESSFUL, got " + status;
            
            // Wait for completion
            future.get();
            
            // Should be SUCCESSFUL
            assert task.status() == Status.SUCCESSFUL : "Final status should be SUCCESSFUL";
        }
    }
    
    private static void testFailingTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new FailingTask("test");
            var future = executor.submit(task);
            
            try {
                future.get();
                assert false : "Should have thrown an exception";
            } catch (Exception e) {
                // Expected
            }
            
            // Give it a moment to update status
            Thread.sleep(100);
            
            assert task.status() == Status.FAILED : "Status should be FAILED after exception";
        }
    }
    
    private static void testCancelTask() throws Exception {
        try (var executor = new TaskExecutor()) {
            var task = new SlowTask("test");
            var future = executor.submit(task);
            
            // Give it a moment to start
            Thread.sleep(100);
            
            // Cancel the task
            task.cancel();
            
            // Give it a moment to process cancellation
            Thread.sleep(100);
            
            assert task.status() == Status.CANCELLED : "Status should be CANCELLED after cancel()";
            assert future.isCancelled() : "Future should be cancelled";
        }
    }
    
    private static void testExecutorClose() throws Exception {
        var executor = new TaskExecutor();
        var task = new SimpleTask("test");
        executor.submit(task);
        
        executor.close();
        
        // After closing, the executor should shutdown
        // We can't directly test the internal state, but we verified it doesn't throw
    }
}
