package dev.enola.be.task;

import dev.enola.be.task.test.ImmediateTask;

public class TaskTest {

    public static void main(String[] args) throws Exception {
        testTaskHasUniqueId();
        testTaskInputIsStored();
        testInitialStatusIsPending();
        testMultipleTasksHaveDifferentIds();
    }
    
    private static void testTaskHasUniqueId() {
        var task = new ImmediateTask("test");
        assert task.id() != null : "Task ID should not be null";
    }
    
    private static void testTaskInputIsStored() {
        var task = new ImmediateTask("test input");
        assert task.input().equals("test input") : "Task input should match";
    }
    
    private static void testInitialStatusIsPending() {
        var task = new ImmediateTask("test");
        assert task.status() == Status.PENDING : "Initial status should be PENDING, got " + task.status();
    }
    
    private static void testMultipleTasksHaveDifferentIds() {
        var task1 = new ImmediateTask("test1");
        var task2 = new ImmediateTask("test2");
        assert !task1.id().equals(task2.id()) : "Different tasks should have different IDs";
    }
}
