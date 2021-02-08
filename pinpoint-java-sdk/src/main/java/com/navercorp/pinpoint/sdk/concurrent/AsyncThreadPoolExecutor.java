package com.navercorp.pinpoint.sdk.concurrent;

import java.util.concurrent.*;

public class AsyncThreadPoolExecutor extends ThreadPoolExecutor {

    public AsyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public AsyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public AsyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public AsyncThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(task);

    }
    public Future<?> submit(Runnable task) {
        return super.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(task,result);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }


}
