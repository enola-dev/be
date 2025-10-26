package dev.enola.be.task;

import static ch.vorburger.test.Assert.assertTrue;

import dev.enola.be.task.test.ImmediateTask;

public class TaskTest {

    public static void main(String[] args) throws Exception {
        testTaskHasNonNullId();
        testTaskInputIsStored();
        testInitialStatusIsPending();
        testMultipleTasksHaveDifferentIds();
    }

    private static void testTaskHasNonNullId() {
        var task = new ImmediateTask("test");
        assertTrue(task.id() != null, "Task ID should not be null");
    }

    private static void testTaskInputIsStored() {
        var task = new ImmediateTask("test input");
        assertTrue("test input".equals(task.input()), "Task input should match");
    }

    private static void testInitialStatusIsPending() {
        var task = new ImmediateTask("test");
        assertTrue(
                task.status() == Status.PENDING,
                "Initial status should be PENDING, got " + task.status());
    }

    private static void testMultipleTasksHaveDifferentIds() {
        var task1 = new ImmediateTask("test1");
        var task2 = new ImmediateTask("test2");
        assertTrue(!task1.id().equals(task2.id()), "Different tasks should have different IDs");
    }
}
