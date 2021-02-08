/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.sdk.v1.concurrent;

import com.navercorp.pinpoint.sdk.v1.concurrent.wrapper.DefaultCommandWrapper;
import com.navercorp.pinpoint.sdk.v1.concurrent.wrapper.CommandWrapper;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Not open yet.
 */
class TraceForkJoinPool extends ForkJoinPool {

    protected CommandWrapper commandWrapper = new DefaultCommandWrapper();

    private TraceForkJoinPool() {
    }

    private TraceForkJoinPool(int parallelism) {
        super(parallelism);
    }


    private TraceForkJoinPool(int parallelism,
                             ForkJoinWorkerThreadFactory factory,
                             Thread.UncaughtExceptionHandler handler,
                             boolean asyncMode) {
        super(parallelism, factory, handler, asyncMode);
    }



    protected <T> ForkJoinTask<T> wrap(ForkJoinTask<T> task) {
        // TODO How to delegate ForkJoinTask?
        return task;
    }



    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        task = wrap(task);
        return super.submit(task);
    }


    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        task = commandWrapper.wrap(task);
        return super.submit(task);
    }


    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        task = commandWrapper.wrap(task);
        return super.submit(task, result);
    }


    public ForkJoinTask<?> submit(Runnable task) {
        task = commandWrapper.wrap(task);
        return super.submit(task);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        tasks = commandWrapper.wrap(tasks);
        return super.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        tasks = commandWrapper.wrap(tasks);
        return super.invokeAny(tasks, timeout, unit);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        tasks = commandWrapper.wrap(tasks);
        return super.invokeAll(tasks, timeout, unit);
    }

    public void execute(ForkJoinTask<?> task) {
        task = wrap(task);
        super.execute(task);
    }


    public void execute(Runnable task) {
        task = commandWrapper.wrap(task);
        super.execute(task);
    }

}
