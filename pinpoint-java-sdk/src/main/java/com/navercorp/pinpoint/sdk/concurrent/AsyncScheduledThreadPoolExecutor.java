package com.navercorp.pinpoint.sdk.concurrent;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class AsyncScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    public AsyncScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public AsyncScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public AsyncScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public AsyncScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    public void execute(Runnable command) {
        schedule(command, 0, NANOSECONDS);
    }


    public Future<?> submit(Runnable task) {
        return super.submit(task);
    }


    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(task, result);
    }


    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(task);
    }


    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay,
                                       TimeUnit unit) {
        return super.schedule(command, delay, unit);

    }


    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay,
                                           TimeUnit unit) {
        return super.schedule(callable, delay, unit);
    }


    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        return super.scheduleAtFixedRate(command, initialDelay, period, unit);
    }


    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit) {
        return super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }


}
