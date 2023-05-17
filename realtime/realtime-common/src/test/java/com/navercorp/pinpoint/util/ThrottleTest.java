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

import java.util.ArrayList;
import java.util.List;
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
        final long threshold = TimeUnit.MILLISECONDS.toNanos(100);
        final long now = System.nanoTime();
        final AtomicLong numTry = new AtomicLong(0);
        final AtomicLong numHit = new AtomicLong(0);
        executeParallel(() -> {
            while (System.nanoTime() - now < threshold) {
                numTry.incrementAndGet();
                if (throttle.hit()) {
                    numHit.incrementAndGet();
                }
            }
        });
        assertThat(numHit.get()).isLessThanOrEqualTo(10).isGreaterThanOrEqualTo(8);
    }

    private void executeParallel(Runnable target) {
        final List<Thread> threads = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            final Thread thread = new Thread(target, "hitTester-" + (i + 1));
            threads.add(thread);
        }
        for (final Thread thread: threads) {
            thread.start();
        }
        for (final Thread thread: threads) {
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (Exception ignored) {}
            }
        }
    }

}
