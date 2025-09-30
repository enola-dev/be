package dev.enola.be.task.demo;

import java.time.Duration;

import dev.enola.be.task.TaskExecutor;
import dev.enola.be.task.Status;
import dev.enola.be.task.demo.LongIncrementingTask.Input;
import dev.enola.be.task.demo.LongIncrementingTask.Output;

import java.util.concurrent.Future;

public class LongIncrementingTaskTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Running LongIncrementingTaskTest...");
        
        testSimpleExecution();
        testCancellation();
        testInputOutput();
        testZeroIterations();
        
        System.out.println("All LongIncrementingTaskTest tests passed! ✓");
    }
    
    private static void testSimpleExecution() throws Exception {
        try (TaskExecutor executor = new TaskExecutor()) {
            Input input = new Input(5, Duration.ofMillis(10));
            LongIncrementingTask task = new LongIncrementingTask(input);
            
            Future<Output> future = executor.submit(task);
            Output output = future.get();
            
            assert output.result() == 5 : "Result should be 5, got " + output.result();
            assert task.status() == Status.SUCCESSFUL : "Status should be SUCCESSFUL";
            
            System.out.println("  ✓ testSimpleExecution");
        }
    }
    
    private static void testCancellation() throws Exception {
        try (TaskExecutor executor = new TaskExecutor()) {
            Input input = new Input(1000, Duration.ofMillis(10)); // Long running
            LongIncrementingTask task = new LongIncrementingTask(input);
            
            Future<Output> future = executor.submit(task);
            
            // Give it a moment to start
            Thread.sleep(50);
            
            // Cancel the task
            task.cancel();
            
            // Give it a moment to process cancellation
            Thread.sleep(100);
            
            assert task.status() == Status.CANCELLED : "Status should be CANCELLED";
            assert future.isCancelled() : "Future should be cancelled";
            
            System.out.println("  ✓ testCancellation");
        }
    }
    
    private static void testInputOutput() throws Exception {
        try (TaskExecutor executor = new TaskExecutor()) {
            Input input = new Input(10, Duration.ofMillis(1));
            LongIncrementingTask task = new LongIncrementingTask(input);
            
            assert task.input().equals(input) : "Task input should match";
            
            Future<Output> future = executor.submit(task);
            Output output = future.get();
            
            assert output.result() == input.max() : "Output result should equal input max";
            
            System.out.println("  ✓ testInputOutput");
        }
    }
    
    private static void testZeroIterations() throws Exception {
        try (TaskExecutor executor = new TaskExecutor()) {
            Input input = new Input(0, Duration.ofMillis(10));
            LongIncrementingTask task = new LongIncrementingTask(input);
            
            Future<Output> future = executor.submit(task);
            Output output = future.get();
            
            assert output.result() == 0 : "Result should be 0 for zero iterations";
            assert task.status() == Status.SUCCESSFUL : "Status should be SUCCESSFUL";
            
            System.out.println("  ✓ testZeroIterations");
        }
    }
}
