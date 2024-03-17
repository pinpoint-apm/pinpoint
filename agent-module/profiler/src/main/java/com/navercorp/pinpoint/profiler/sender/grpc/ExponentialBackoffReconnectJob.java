/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import io.github.resilience4j.core.IntervalFunction;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ExponentialBackoffReconnectJob implements ReconnectJob {

    private static final AtomicIntegerFieldUpdater<ExponentialBackoffReconnectJob> ATTEMPT_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(ExponentialBackoffReconnectJob.class, "attempt");
    private volatile int attempt = 0;

    private final IntervalFunction intervalFunction;
    private final Runnable runnable;
    private volatile long lastInterval; // for logging

    public ExponentialBackoffReconnectJob(Runnable runnable) {
        this(runnable, TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(30));
    }

    public ExponentialBackoffReconnectJob(Runnable runnable, long initialIntervalMillis, long maxIntervalMillis) {
        this.runnable = Objects.requireNonNull(runnable, "runnable");
        this.intervalFunction = IntervalFunction.ofExponentialRandomBackoff(initialIntervalMillis, 1.2, 0.3, maxIntervalMillis);
        this.lastInterval = initialIntervalMillis;
    }


    @Override
    public final void resetInterval() {
        ATTEMPT_UPDATER.set(this, 0);
    }

    @Override
    public long nextInterval() {
        final int attempt = ATTEMPT_UPDATER.incrementAndGet(this);
        final long interval = intervalFunction.apply(attempt);
        this.lastInterval = interval;
        return interval;
    }

    @VisibleForTesting
    int getAttempt() {
        return this.attempt;
    }

    @Override
    public void run() {
        this.runnable.run();
    }

    @Override
    public String toString() {
        return "ExponentialBackoffReconnectJob{" +
                "attempt=" + attempt +
                ", lastInterval=" + lastInterval + "ms" +
                ", runnable=" + runnable +
                '}';
    }
}
