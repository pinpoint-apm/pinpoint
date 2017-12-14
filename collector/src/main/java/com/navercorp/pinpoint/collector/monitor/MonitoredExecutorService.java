/*
 * Copyright 2016 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.collector.monitor;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * refer to dropwizard metrics
 * https://github.com/dropwizard/metrics/blob/3.2-development/metrics-core/src/main/java/com/codahale/metrics/InstrumentedExecutorService.java
 *
 * Caution : If using multiple method (like invokeAny, invokeAll), then rejected count can be incorrect.
 *
 * @author Taejin Koo
 */
public class MonitoredExecutorService implements ExecutorService {

    private final Meter submitted;
    private final Meter rejected;
    private final Counter running;
    private final Meter completed;
    private final Timer dispatchDurationTimer;
    private final Timer durationTimer;

    private final ExecutorService delegate;

    public MonitoredExecutorService(ExecutorService delegate, MetricRegistry registry, String name) {
        this.delegate = delegate;

        this.submitted = registry.meter(MetricRegistry.name(name, "submitted"));
        this.rejected = registry.meter(MetricRegistry.name(name, "rejected"));
        this.running = registry.counter(MetricRegistry.name(name, "running"));
        this.completed = registry.meter(MetricRegistry.name(name, "completed"));

        this.dispatchDurationTimer = registry.timer(MetricRegistry.name(name, "dispatchDuration"));
        this.durationTimer = registry.timer(MetricRegistry.name(name, "duration"));
    }

    @Override
    public void execute(Runnable runnable) {
        submitted.mark();
        try {
            delegate.execute(new InstrumentedRunnable(runnable));
        } catch (RejectedExecutionException ree) {
            rejected.mark();
            throw ree;
        }
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        submitted.mark();
        try {
            return delegate.submit(new InstrumentedRunnable(runnable));
        } catch (RejectedExecutionException ree) {
            rejected.mark();
            throw ree;
        }
    }

    @Override
    public <T> Future<T> submit(Runnable runnable, T result) {
        submitted.mark();
        try {
            return delegate.submit(new InstrumentedRunnable(runnable), result);
        } catch (RejectedExecutionException ree) {
            rejected.mark();
            throw ree;
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        submitted.mark();
        try {
            return delegate.submit(new InstrumentedCallable<T>(callable));
        } catch (RejectedExecutionException ree) {
            rejected.mark();
            throw ree;
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables) throws InterruptedException {
        submitted.mark(callables.size());
        Collection<? extends Callable<T>> instrumented = instrument(callables);
        try {
            return delegate.invokeAll(instrumented);
        } catch (RejectedExecutionException ree) {
            rejected.mark(callables.size());
            throw ree;
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables, long timeout, TimeUnit unit) throws InterruptedException {
        submitted.mark(callables.size());
        Collection<? extends Callable<T>> instrumented = instrument(callables);
        try {
            return delegate.invokeAll(instrumented, timeout, unit);
        } catch (RejectedExecutionException ree) {
            rejected.mark(callables.size());
            throw ree;
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> callables) throws ExecutionException, InterruptedException {
        submitted.mark(callables.size());
        Collection<? extends Callable<T>> instrumented = instrument(callables);
        try {
            return delegate.invokeAny(instrumented);
        } catch (RejectedExecutionException ree) {
            rejected.mark(callables.size());
            throw ree;
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> callables, long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        submitted.mark(callables.size());
        Collection<? extends Callable<T>> instrumented = instrument(callables);
        try {
            return delegate.invokeAny(instrumented, timeout, unit);
        } catch (RejectedExecutionException ree) {
            rejected.mark(callables.size());
            throw ree;
        }
    }

    private <T> Collection<? extends Callable<T>> instrument(Collection<? extends Callable<T>> callables) {
        final List<InstrumentedCallable<T>> instrumented = new ArrayList<InstrumentedCallable<T>>(callables.size());
        for (Callable<T> callable : callables) {
            instrumented.add(new InstrumentedCallable<T>(callable));
        }
        return instrumented;
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
    public boolean awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return delegate.awaitTermination(timeout, timeUnit);
    }

    private class InstrumentedRunnable implements Runnable {
        private final Runnable runnable;
        private final Timer.Context dispatchDuration;

        InstrumentedRunnable(Runnable runnable) {
            this.runnable = runnable;
            this.dispatchDuration = dispatchDurationTimer.time();
        }

        @Override
        public void run() {
            dispatchDuration.stop();

            running.inc();
            final Timer.Context context = durationTimer.time();
            try {
                runnable.run();
            } finally {
                context.stop();
                running.dec();
                completed.mark();
            }
        }
    }

    private class InstrumentedCallable<T> implements Callable<T> {
        private final Callable<T> callable;
        private final Timer.Context dispatchDuration;

        InstrumentedCallable(Callable<T> callable) {
            this.callable = callable;
            this.dispatchDuration = dispatchDurationTimer.time();
        }

        @Override
        public T call() throws Exception {
            dispatchDuration.stop();

            running.inc();
            final Timer.Context context = durationTimer.time();
            try {
                return callable.call();
            } finally {
                context.stop();
                running.dec();
                completed.mark();
            }
        }
    }

}
