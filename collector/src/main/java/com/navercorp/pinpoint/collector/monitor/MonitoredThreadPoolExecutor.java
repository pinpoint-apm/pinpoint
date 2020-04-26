/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.monitor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MonitoredThreadPoolExecutor extends ThreadPoolExecutor {

    private final RunnableDecorator runnableDecorator;

    public MonitoredThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                       RunnableDecorator runnableDecorator) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.runnableDecorator = Objects.requireNonNull(runnableDecorator, "executeListener");

    }


    public MonitoredThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                       ThreadFactory threadFactory, RunnableDecorator runnableDecorator) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.runnableDecorator = Objects.requireNonNull(runnableDecorator, "runnableDecorator");
    }

    public MonitoredThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                       RejectedExecutionHandler handler, RunnableDecorator runnableDecorator) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        this.runnableDecorator = Objects.requireNonNull(runnableDecorator, "runnableDecorator");

    }

    public MonitoredThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                       ThreadFactory threadFactory, RejectedExecutionHandler handler, RunnableDecorator runnableDecorator) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.runnableDecorator = Objects.requireNonNull(runnableDecorator, "runnableDecorator");
    }

    @Override
    public void execute(Runnable command) {
        super.execute(runnableDecorator.decorate(command));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return super.invokeAny(runnableDecorator.decorate(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return super.invokeAny(runnableDecorator.decorate(tasks), timeout, unit);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return super.invokeAll(runnableDecorator.decorate(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return super.invokeAll(runnableDecorator.decorate(tasks), timeout, unit);
    }

}

