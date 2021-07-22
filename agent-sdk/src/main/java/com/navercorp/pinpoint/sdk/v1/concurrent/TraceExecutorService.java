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

import com.navercorp.pinpoint.sdk.v1.concurrent.wrapper.CommandWrapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/*
 * {@link ExecutorService} for TraceContext propagation.
 * <p>{@link TraceExecutorService} marks the entry point of the async action.
 */

public class TraceExecutorService implements ExecutorService {

    protected final ExecutorService delegate;
    protected final CommandWrapper wrapper;

    public TraceExecutorService(ExecutorService delegate, CommandWrapper wrapper) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.wrapper = Objects.requireNonNull(wrapper, "wrapper");
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Objects.requireNonNull(task, "task");

        task = wrapper.wrap(task);
        return delegate.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        Objects.requireNonNull(task, "task");

        task = wrapper.wrap(task);
        return delegate.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        Objects.requireNonNull(task, "task");

        task = wrapper.wrap(task);
        return delegate.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        Objects.requireNonNull(tasks, "tasks");

        tasks = wrapper.wrap(tasks);
        return delegate.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(tasks, "tasks");

        tasks = wrapper.wrap(tasks);
        return delegate.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        Objects.requireNonNull(tasks, "tasks");

        tasks = wrapper.wrap(tasks);
        return delegate.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Objects.requireNonNull(tasks, "tasks");

        tasks = wrapper.wrap(tasks);
        return delegate.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        Objects.requireNonNull(command, "command");

        command = wrapper.wrap(command);
        delegate.execute(command);
    }

}
