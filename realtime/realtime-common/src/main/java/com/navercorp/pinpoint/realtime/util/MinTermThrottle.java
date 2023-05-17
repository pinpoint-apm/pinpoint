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
package com.navercorp.pinpoint.realtime.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author youngjin.kim2
 */
public class MinTermThrottle implements Throttle {

    private final AtomicLong lastHit = new AtomicLong(0);
    private final long minTermNanos;

    public MinTermThrottle(long minTermNanos) {
        this.minTermNanos = minTermNanos;
    }

    @Override
    public boolean hit() {
        final long now = System.nanoTime();
        final long prev = lastHit.get();
        if (now - prev >= this.minTermNanos) {
            return lastHit.compareAndSet(prev, now);
        }
        return false;
    }

}
