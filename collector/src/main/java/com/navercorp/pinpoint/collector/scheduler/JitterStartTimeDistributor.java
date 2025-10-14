/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.scheduler;

import java.util.concurrent.ThreadLocalRandom;

public class JitterStartTimeDistributor implements StartTimeDistributor {
    private final long nextDelay;
    private final double spread;

    private long nextTick = 0;

    public JitterStartTimeDistributor(long nextDelay, double spread) {
        this.nextDelay = nextDelay;
        this.spread = spread;
    }

    public long nextTick() {
        nextTick = nextTick + nextDelay;
        return nextTick + jitter();
    }

    private long jitter() {
        long spreadTime = (long)((double)nextDelay * spread);

        return ThreadLocalRandom.current().nextLong(-spreadTime, spreadTime);
    }

    @Override
    public String toString() {
        return "JitterStartTimeDistributor{" +
               ", nextDelay=" + nextDelay +
               ", spread=" + spread +
               ", nextTick=" + nextTick +
               '}';
    }
}
