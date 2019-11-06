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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
/**
 * @author Woonduk Kang(emeroad)
 */
public class MonitoredRunnableDecorator implements RunnableDecorator {

    @SuppressWarnings("unused") // for debug
    private final String executorName;
    private final Meter submitted;

    private final Timer dispatchDurationTimer;
    private final Timer durationTimer;
    private final MetricRegistry registry;

    public MonitoredRunnableDecorator(String executorName, MetricRegistry registry) {
        this.registry = registry;
        this.executorName = Objects.requireNonNull(executorName, "name");

        this.submitted = registry.meter(MetricRegistry.name(executorName, "submitted"));

        this.dispatchDurationTimer = registry.timer(MetricRegistry.name(executorName, "dispatchDuration"));
        this.durationTimer = registry.timer(MetricRegistry.name(executorName, "duration"));
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        if (runnable == null) {
            throw new NullPointerException("runnable");
        }
        submitted.mark();
        return instrument(runnable);
    }

    private Runnable instrument(Runnable runnable) {
        return new InstrumentedRunnable(runnable);
    }


    @Override
    public <T> Callable<T> decorate(Callable<T> task) {
        if (task == null) {
            throw new NullPointerException("task");
        }

        submitted.mark();
        return instrument(task);
    }

    private <V> Callable<V> instrument(Callable<V> runnable) {
        return new InstrumentedCallable<>(runnable);
    }


    @Override
    public <T> Collection<? extends Callable<T>> decorate(Collection<? extends Callable<T>> tasks) {
        if (tasks == null) {
            throw new NullPointerException("tasks");
        }
        submitted.mark(tasks.size());
        return instrument(tasks);
    }

    private <T> Collection<? extends Callable<T>> instrument(Collection<? extends Callable<T>> tasks) {
        final List<Callable<T>> instrumented = new ArrayList<Callable<T>>(tasks.size());
        for (Callable<T> callable : tasks) {
            instrumented.add(instrument(callable));
        }
        return instrumented;
    }


    private class InstrumentedRunnable implements Runnable {
        private final Runnable runnable;
        private final Timer.Context dispatchDuration;

        InstrumentedRunnable(Runnable runnable) {
            this.runnable = Objects.requireNonNull(runnable, "runnable");
            this.dispatchDuration = dispatchDurationTimer.time();
        }

        @Override
        public void run() {
            dispatchDuration.stop();

            final Timer.Context context = durationTimer.time();
            try {
                runnable.run();
            } finally {
                context.stop();
            }
        }

        @Override
        public String toString() {
            return runnable.toString();
        }
    }


    private class InstrumentedCallable<T> implements Callable<T> {
        private final Callable<T> callable;
        private final Timer.Context dispatchDuration;

        InstrumentedCallable(Callable<T> callable) {
            this.callable = Objects.requireNonNull(callable, "callable");
            this.dispatchDuration = dispatchDurationTimer.time();
        }

        @Override
        public T call() throws Exception {
            dispatchDuration.stop();

            final Timer.Context context = durationTimer.time();
            try {
                return callable.call();
            } finally {
                context.stop();
            }
        }

        @Override
        public String toString() {
            return callable.toString();
        }
    }
}

