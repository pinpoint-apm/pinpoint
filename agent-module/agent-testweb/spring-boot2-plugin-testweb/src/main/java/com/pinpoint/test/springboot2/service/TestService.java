package com.pinpoint.test.springboot2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class TestService {

    private final ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public TestService(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Async
    public CompletableFuture<String> getHelloAsync() {
        return CompletableFuture.completedFuture("hello");
    }

    @Async
    public Future<String> getHelloFuture() {
        return CompletableFuture.completedFuture("hello-future");
    }

    @Async
    public void fireAndForget() {
        // void @Async path: AsyncExecutionAspectSupport.doSubmit -> AsyncTaskExecutor.submit
    }

    public CompletableFuture<String> submitCompletableDirect() {
        // ThreadPoolTaskExecutor.submitCompletable is Spring 6+, use supplyAsync on Spring 5
        return CompletableFuture.supplyAsync(() -> "direct-completable", taskExecutor);
    }

    public Future<String> submitDirect() {
        return taskExecutor.submit(() -> "direct-submit");
    }
}
