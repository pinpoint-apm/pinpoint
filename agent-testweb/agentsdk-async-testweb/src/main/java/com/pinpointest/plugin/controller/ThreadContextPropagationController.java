package com.pinpointest.plugin.controller;

import com.navercorp.pinpoint.sdk.v1.concurrent.TraceCallable;
import com.navercorp.pinpoint.sdk.v1.concurrent.TraceExecutors;
import com.navercorp.pinpoint.sdk.v1.concurrent.TraceRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
public class ThreadContextPropagationController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService traceExecutor = TraceExecutors.wrapExecutorService(Executors.newSingleThreadExecutor());

    @GetMapping(value = "/sdk-async-plugin/manual-context-propagation")
    public String manualWrapAndExecute() throws Exception {

        CompletableFuture<String> future = new CompletableFuture<>();

        traceExecutor.execute(TraceRunnable.wrap(() -> future.complete("manual-execute")));

        Thread.sleep(1000);

        return future.get();
    }

    private final ExecutorService contextPropagationExecutor = TraceExecutors.wrapExecutorService(Executors.newSingleThreadExecutor(), true);

    @GetMapping(value = "/sdk-async-plugin/auto-context-propagation")
    public String autoWrapAndExecute() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();

        contextPropagationExecutor.execute(() -> future.complete("auto-execute"));

        Thread.sleep(1000);

        return future.get();
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @GetMapping(value = "/sdk-async-plugin/asyncEntry-propagation")
    public String asyncEntryAndExecute() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();

        Runnable command = TraceRunnable.asyncEntry(() -> future.complete("asyncEntry-execute"));
        executor.execute(command);

        Thread.sleep(1000);

        return future.get();
    }

    @GetMapping(value = "/sdk-async-plugin/call")
    public String manualWrapAndCall() throws Exception {
        Callable<String> command = TraceCallable.wrap(() -> "asyncEntry-execute1");
        Future<String> future = traceExecutor.submit(command);

        Thread.sleep(1000);

        return future.get(3000, TimeUnit.MILLISECONDS);
    }

    @GetMapping(value = "/sdk-async-plugin/asyncEntryCall")
    public String manualWrapAndAsyncEntryCall() throws Exception {
        Callable<String> command = TraceCallable.asyncEntry(() -> "asyncEntry-execute1");
        Future<String> future = executor.submit(command);

        Thread.sleep(1000);

        return future.get(3000, TimeUnit.MILLISECONDS);
    }


    @GetMapping(value = "/sdk-async-plugin/invokeAll")
    public String invokeAll() throws Exception {
        Callable<String> command1 = TraceCallable.asyncEntry(() -> "asyncEntry-execute1");
        Callable<String> command2 = TraceCallable.asyncEntry(() -> "asyncEntry-execute1");
        List<Future<String>> futures = executor.invokeAll(Arrays.asList(command1, command2));

        Thread.sleep(1000);

        return futures.get(0).get(3000, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    private void shutdown() {
        this.traceExecutor.shutdown();
        this.contextPropagationExecutor.shutdown();
        this.executor.shutdown();
    }
}
