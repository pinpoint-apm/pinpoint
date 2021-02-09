package com.binpo.pinpoint.sdk.concurrent;

import java.util.concurrent.*;

public class AsyncForkJoinPool extends ForkJoinPool {

    private static final long DEFAULT_KEEPALIVE = 60_000L;
    static final int MAX_CAP      = 0x7fff;        // max #workers - 1

    public AsyncForkJoinPool(int parallelism,
                        ForkJoinWorkerThreadFactory factory,
                        Thread.UncaughtExceptionHandler handler,
                        boolean asyncMode) {
        super(parallelism,factory,handler,asyncMode);
    }


    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        return super.submit(task);
    }


    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        return super.submit(task);
    }


    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        return super.submit(task,result);
    }


    public ForkJoinTask<?> submit(Runnable task) {
        return super.submit(task);
    }

    public void execute(ForkJoinTask<?> task) {
        super.execute(task);
    }


    public void execute(Runnable task) {
        super.execute(task);

    }

}
