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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Allows one log per {@code ratio} calls.
 *
 * @author Woonduk Kang(emeroad)
 * @deprecated call-count throttling emits in bursts under load spikes; use {@link CountingTimeLogThrottle} instead
 */
@Deprecated
public class CountLogThrottle implements LogThrottle {

    private final AtomicLong counter = new AtomicLong();

    private final long ratio;

    public CountLogThrottle(long ratio) {
        this.ratio = Math.max(ratio, 1);
    }

    @Override
    public boolean tryAcquire() {
        return counter.getAndIncrement() % ratio == 0;
    }

    @Override
    public long getCounter() {
        return counter.get();
    }
}
