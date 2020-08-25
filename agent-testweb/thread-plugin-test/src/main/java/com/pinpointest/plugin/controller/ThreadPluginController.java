package com.pinpointest.plugin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@RestController
public class ThreadPluginController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping(value = "/thread-plugin/supplyAsync")
    public String completableFutureSupplyAsync() throws InterruptedException, ExecutionException, TimeoutException {
        Supplier<String> supplier =() -> threadPool();

        CompletableFuture<String> future = CompletableFuture.supplyAsync(supplier, executorService);

        sleep(1000);

        return future.get();
    }

    @GetMapping(value = "/thread-plugin/complete")
    public String completableFutureSupplyAsync2() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> future = new CompletableFuture();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                future.complete("threadPool-execute");
            }
        });
        return future.get();
    }

    @GetMapping(value = "/thread-plugin/springAsync")
    public String completableFutureSpringAsync() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = springAsyncMethod();

        return future.get();
    }



    @Async
    public CompletableFuture<String> springAsyncMethod() throws InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();

        sleep(1000);

        future.complete("springAsync");
        return future;
    }

    @GetMapping(value = "/thread-plugin/runAsync")
    public String completableFutureRunAsync() throws InterruptedException, ExecutionException, TimeoutException {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                threadPool();
            }
        };
        CompletableFuture<Void> future = CompletableFuture.runAsync(runnable);
        Void aVoid = future.get(2000, TimeUnit.MICROSECONDS);

        return "void";
    }


    public String threadPool() {
        logger.info("threadId:{} test", Thread.currentThread().getId());
        return "test";
    }

    private void sleep(long millis) throws InterruptedException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    private void shutdown() {
        this.executorService.shutdown();
    }
}
