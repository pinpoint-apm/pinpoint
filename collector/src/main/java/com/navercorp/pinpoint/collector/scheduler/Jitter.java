/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.scheduler;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Jitter implementation based on HBase's JitterScheduledThreadPoolExecutorImpl.
 * Adds random jitter to delays to spread out scheduled tasks and reduce load spikes.
 * 
 * @author emeroad
 */
public class Jitter {
    private final double spread;

    /**
     * Creates a Jitter with the specified spread percentage.
     * 
     * @param spread The percent up and down that delays should be jittered (e.g., 0.5 for 50%)
     */
    public Jitter(double spread) {
        if (spread < 0 || spread > 1.0) {
            throw new IllegalArgumentException("spread must be between 0 and 1.0");
        }
        this.spread = spread;
    }

    /**
     * Calculates jittered delay based on the base delay.
     * The jitter is applied symmetrically: baseDelay +/- (baseDelay * spread)
     * 
     * @param baseDelay the base delay in milliseconds
     * @return the jittered delay, guaranteed to be non-negative
     */
    public long nextDelay(long baseDelay) {
        long spreadTime = (long) (baseDelay * spread);
        if (spreadTime <= 0) {
            return baseDelay;
        }
        
        // Add random jitter in range [-spreadTime, +spreadTime]
        long jitter = ThreadLocalRandom.current().nextLong(-spreadTime, spreadTime + 1);
        long delay = baseDelay + jitter;
        
        // Ensure we don't return negative delay
        return Math.max(0, delay);
    }
}
