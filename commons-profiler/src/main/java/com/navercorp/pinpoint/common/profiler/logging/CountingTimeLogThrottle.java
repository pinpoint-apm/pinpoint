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
 * {@link TimeLogThrottle} that also counts calls;
 * suppressed calls are still counted and visible through {@link #getCounter()}.
 *
 * @author Woonduk Kang(emeroad)
 */
public class CountingTimeLogThrottle extends TimeLogThrottle {
    private static final AtomicLongFieldUpdater<CountingTimeLogThrottle> COUNTER
            = AtomicLongFieldUpdater.newUpdater(CountingTimeLogThrottle.class, "counter");

    private volatile long counter;

    public CountingTimeLogThrottle(long intervalMillis) {
        super(intervalMillis);
    }

    CountingTimeLogThrottle(long intervalMillis, LongSupplier clock) {
        super(intervalMillis, clock);
    }

    @Override
    public boolean tryAcquire() {
        COUNTER.getAndIncrement(this);
        return super.tryAcquire();
    }

    @Override
    public long getCounter() {
        return COUNTER.get(this);
    }
}
