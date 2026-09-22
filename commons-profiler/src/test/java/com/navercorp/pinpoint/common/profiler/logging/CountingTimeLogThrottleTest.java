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

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CountingTimeLogThrottleTest {

    @Test
    public void firstCallAlwaysLogs() {
        AtomicLong clock = new AtomicLong(1000);
        CountingTimeLogThrottle throttle = new CountingTimeLogThrottle(3000, clock::get);

        assertThat(throttle.tryAcquire()).isTrue();
    }

    @Test
    public void suppressWithinInterval() {
        AtomicLong clock = new AtomicLong(1000);
        CountingTimeLogThrottle throttle = new CountingTimeLogThrottle(3000, clock::get);

        assertThat(throttle.tryAcquire()).isTrue();

        clock.set(2000);
        assertThat(throttle.tryAcquire()).isFalse();

        clock.set(3999);
        assertThat(throttle.tryAcquire()).isFalse();

        // interval elapsed
        clock.set(4000);
        assertThat(throttle.tryAcquire()).isTrue();
        assertThat(throttle.tryAcquire()).isFalse();
    }

    @Test
    public void countSuppressedCalls() {
        AtomicLong clock = new AtomicLong(1000);
        CountingTimeLogThrottle throttle = new CountingTimeLogThrottle(3000, clock::get);

        throttle.tryAcquire();
        throttle.tryAcquire();
        throttle.tryAcquire();

        assertThat(throttle.getCounter()).isEqualTo(3);
    }

    @Test
    public void invalidInterval() {
        assertThatThrownBy(() -> new CountingTimeLogThrottle(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CountingTimeLogThrottle(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
