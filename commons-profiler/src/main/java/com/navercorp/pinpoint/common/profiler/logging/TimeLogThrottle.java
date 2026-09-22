/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.common.profiler.logging;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.LongSupplier;

/**
 * Allows one log per {@code intervalMillis}; the first call always logs.
 * Calls are not counted, so a burst of suppressed calls only reads a volatile field
 * instead of contending on a shared counter, and {@link #getCounter()} always returns
 * {@link LogThrottle#DISABLED_COUNTER}.
 * <p>
 * See {@link CountingTimeLogThrottle} for the counting variant.
 *
 * @author Woonduk Kang(emeroad)
 */
public class TimeLogThrottle implements LogThrottle {
    private static final AtomicLongFieldUpdater<TimeLogThrottle> NEXT_LOG_TIME
            = AtomicLongFieldUpdater.newUpdater(TimeLogThrottle.class, "nextLogTime");

    private volatile long nextLogTime;

    private final long intervalMillis;
    private final LongSupplier clock;

    public TimeLogThrottle(long intervalMillis) {
        this(intervalMillis, System::currentTimeMillis);
    }

    TimeLogThrottle(long intervalMillis, LongSupplier clock) {
        if (intervalMillis <= 0) {
            throw new IllegalArgumentException("intervalMillis must be positive: " + intervalMillis);
        }
        this.intervalMillis = intervalMillis;
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire() {

        final long now = clock.getAsLong();
        final long next = this.nextLogTime;
        if (now < next) {
            return false;
        }
        // CAS makes a single winner per interval under concurrency
        return NEXT_LOG_TIME.compareAndSet(this, next, now + intervalMillis);
    }

    @Override
    public long getCounter() {
        return DISABLED_COUNTER;
    }
}