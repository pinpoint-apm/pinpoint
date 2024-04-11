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
package com.navercorp.pinpoint.util;

import com.navercorp.pinpoint.realtime.util.MinTermThrottle;
import com.navercorp.pinpoint.realtime.util.Throttle;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class ThrottleTest {

    @Test
    public void shouldHitAround10Times() {
        final Throttle throttle = new MinTermThrottle(TimeUnit.MILLISECONDS.toNanos(10));
        final long testDuration = TimeUnit.MILLISECONDS.toNanos(100);
        final long startedAt = System.nanoTime();
        final AtomicLong numTry = new AtomicLong(0);
        final AtomicLong numHit = new AtomicLong(0);
        executeParallel(() -> {
            while (System.nanoTime() - startedAt < testDuration) {
                numTry.incrementAndGet();
                if (throttle.hit()) {
                    numHit.incrementAndGet();
                }
            }
        });
        assertThat(numHit.get()).isLessThanOrEqualTo(11).isGreaterThanOrEqualTo(8);
    }

    @SuppressWarnings("unchecked")
    private void executeParallel(Runnable target) {
        final CompletableFuture<Void>[] result = new CompletableFuture[4];
        for (int i = 0; i < 4; i++) {
            result[i] = CompletableFuture.runAsync(target);
        }
        CompletableFuture.allOf(result).join();
    }

}
