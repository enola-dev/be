package dev.enola.be.task;

import java.util.UUID;

public class TaskTest {

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
            Thread.sleep(5000); // Sleep for 5 seconds
            return "Completed: " + input;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running TaskTest...");
        
        testTaskHasUniqueId();
        testTaskInputIsStored();
        testInitialStatusIsPending();
        testTaskIdIsNotNull();
        testMultipleTasksHaveDifferentIds();
        
        System.out.println("All TaskTest tests passed! ✓");
    }
    
    private static void testTaskHasUniqueId() {
        SimpleTask task = new SimpleTask("test");
        UUID id = task.id();
        assert id != null : "Task ID should not be null";
        System.out.println("  ✓ testTaskHasUniqueId");
    }
    
    private static void testTaskInputIsStored() {
        String input = "test input";
        SimpleTask task = new SimpleTask(input);
        assert task.input().equals(input) : "Task input should match";
        System.out.println("  ✓ testTaskInputIsStored");
    }
    
    private static void testInitialStatusIsPending() {
        SimpleTask task = new SimpleTask("test");
        Status status = task.status();
        assert status == Status.PENDING : "Initial status should be PENDING, got " + status;
        System.out.println("  ✓ testInitialStatusIsPending");
    }
    
    private static void testTaskIdIsNotNull() {
        SimpleTask task = new SimpleTask("test");
        assert task.id() != null : "Task ID should never be null";
        System.out.println("  ✓ testTaskIdIsNotNull");
    }
    
    private static void testMultipleTasksHaveDifferentIds() {
        SimpleTask task1 = new SimpleTask("test1");
        SimpleTask task2 = new SimpleTask("test2");
        assert !task1.id().equals(task2.id()) : "Different tasks should have different IDs";
        System.out.println("  ✓ testMultipleTasksHaveDifferentIds");
    }
}
