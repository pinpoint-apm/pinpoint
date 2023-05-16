package com.navercorp.pinpoint.common.hbase.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SharedExecutorService implements ExecutorService {
    private final ExecutorService e;
    private boolean shutdown = false;

    public SharedExecutorService(ExecutorService e) {
        this.e = Objects.requireNonNull(e, "e");
    }

    @Override
    public void shutdown() {
        this.shutdown = true;
//        e.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        this.shutdown = true;
//        return e.shutdownNow();
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return this.shutdown;
    }

    @Override
    public boolean isTerminated() {
        return this.shutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
//        return e.awaitTermination(timeout, unit);
        return true;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return e.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return e.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return e.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return e.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return e.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return e.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return e.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        e.execute(command);
    }
}
