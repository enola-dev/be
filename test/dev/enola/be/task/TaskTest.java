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
        testTaskHasUniqueId();
        testTaskInputIsStored();
        testInitialStatusIsPending();
        testTaskIdIsNotNull();
        testMultipleTasksHaveDifferentIds();
    }
    
    private static void testTaskHasUniqueId() {
        var task = new SimpleTask("test");
        var id = task.id();
        assert id != null : "Task ID should not be null";
    }
    
    private static void testTaskInputIsStored() {
        var input = "test input";
        var task = new SimpleTask(input);
        assert task.input().equals(input) : "Task input should match";
    }
    
    private static void testInitialStatusIsPending() {
        var task = new SimpleTask("test");
        var status = task.status();
        assert status == Status.PENDING : "Initial status should be PENDING, got " + status;
    }
    
    private static void testTaskIdIsNotNull() {
        var task = new SimpleTask("test");
        assert task.id() != null : "Task ID should never be null";
    }
    
    private static void testMultipleTasksHaveDifferentIds() {
        var task1 = new SimpleTask("test1");
        var task2 = new SimpleTask("test2");
        assert !task1.id().equals(task2.id()) : "Different tasks should have different IDs";
    }
}
