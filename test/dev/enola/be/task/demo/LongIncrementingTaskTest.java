package dev.enola.be.task.demo;

import java.time.Duration;

import dev.enola.be.task.TaskExecutor;
import dev.enola.be.task.Status;
import dev.enola.be.task.demo.LongIncrementingTask.Input;
import dev.enola.be.task.demo.LongIncrementingTask.Output;

import java.util.concurrent.Future;

public class LongIncrementingTaskTest {

    public static void main(String[] args) throws Exception {
        testSimpleExecution();
        testCancellation();
        testInputOutput();
        testZeroIterations();
    }
    
    private static void testSimpleExecution() throws Exception {
        try (var executor = new TaskExecutor()) {
            var input = new Input(5, Duration.ofMillis(10));
            var task = new LongIncrementingTask(input);
            
            var future = executor.submit(task);
            var output = future.get();
            
            assert output.result() == 5 : "Result should be 5, got " + output.result();
            assert task.status() == Status.SUCCESSFUL : "Status should be SUCCESSFUL";
        }
    }
    
    private static void testCancellation() throws Exception {
        try (var executor = new TaskExecutor()) {
            var input = new Input(1000, Duration.ofMillis(10)); // Long running
            var task = new LongIncrementingTask(input);
            
            var future = executor.submit(task);
            
            // Give it a moment to start
            Thread.sleep(50);
            
            // Cancel the task
            task.cancel();
            
            // Give it a moment to process cancellation
            Thread.sleep(100);
            
            assert task.status() == Status.CANCELLED : "Status should be CANCELLED";
            assert future.isCancelled() : "Future should be cancelled";
        }
    }
    
    private static void testInputOutput() throws Exception {
        try (var executor = new TaskExecutor()) {
            var input = new Input(10, Duration.ofMillis(1));
            var task = new LongIncrementingTask(input);
            
            assert task.input().equals(input) : "Task input should match";
            
            var future = executor.submit(task);
            var output = future.get();
            
            assert output.result() == input.max() : "Output result should equal input max";
        }
    }
    
    private static void testZeroIterations() throws Exception {
        try (var executor = new TaskExecutor()) {
            var input = new Input(0, Duration.ofMillis(10));
            var task = new LongIncrementingTask(input);
            
            var future = executor.submit(task);
            var output = future.get();
            
            assert output.result() == 0 : "Result should be 0 for zero iterations";
            assert task.status() == Status.SUCCESSFUL : "Status should be SUCCESSFUL";
        }
    }
}
