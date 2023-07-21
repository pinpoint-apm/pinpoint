/*
 * Copyright 2023 NAVER Corp.
 *
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
 */
package com.navercorp.pinpoint.web.realtime.activethread.count.service;

import reactor.core.Disposable;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * @author youngjin.kim2
 */
public class PeriodicRunner {
    private final ScheduledExecutorService scheduledExecutorService;

    private Supplier<Boolean> predicate;

    private PeriodicRunner(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public static PeriodicRunner executedBy(ScheduledExecutorService scheduledExecutorService) {
        return new PeriodicRunner(scheduledExecutorService);
    }

    public PeriodicRunner continueWhen(Supplier<Boolean> predicate) {
        this.predicate = predicate;
        return this;
    }

    public void runWithFixedDelay(Runnable child, Duration delay, Duration period) {
        PeriodicRunnable periodicRunnable =
                new PeriodicRunnable(predicate, scheduledExecutorService, child, period.toMillis());
        scheduledExecutorService.schedule(periodicRunnable, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private static class PeriodicRunnable implements Runnable, Disposable {

        private final ScheduledExecutorService scheduledExecutorService;
        private final Supplier<Boolean> predicate;
        private final Runnable child;
        private final long periodMs;

        private final AtomicBoolean disposed = new AtomicBoolean(false);

        private PeriodicRunnable(
                Supplier<Boolean> predicate,
                ScheduledExecutorService scheduledExecutorService,
                Runnable child,
                long periodMs
        ) {
            this.predicate = predicate;
            this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService, "scheduledExecutorService");
            this.child = Objects.requireNonNull(child, "child");
            this.periodMs = periodMs;
        }

        @Override
        public void run() {
            if (this.predicate != null && !this.predicate.get()) {
                dispose();
                return;
            }

            if (this.isDisposed()) {
                return;
            }

            this.child.run();
            this.scheduledExecutorService.schedule(this, periodMs, TimeUnit.MILLISECONDS);
        }

        @Override
        public void dispose() {
            this.disposed.set(true);
        }

        @Override
        public boolean isDisposed() {
            return this.disposed.get();
        }

    }

}
