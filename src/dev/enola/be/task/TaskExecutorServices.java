package dev.enola.be.task;

import java.util.concurrent.ExecutorService;

final class TaskExecutorServices {

    static ExecutorService newVirtualThreadPerTaskExecutor() {
        return java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
    }

    private TaskExecutorServices() {}
}
