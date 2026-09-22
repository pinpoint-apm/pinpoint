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

import static org.assertj.core.api.Assertions.assertThat;

public class CountLogThrottleTest {

    @Test
    public void allowEveryNthCall() {
        CountLogThrottle throttle = new CountLogThrottle(3);

        assertThat(throttle.tryAcquire()).isTrue();
        assertThat(throttle.tryAcquire()).isFalse();
        assertThat(throttle.tryAcquire()).isFalse();
        assertThat(throttle.tryAcquire()).isTrue();
        assertThat(throttle.tryAcquire()).isFalse();
    }

    @Test
    public void nonPositiveRatioAlwaysAllows() {
        CountLogThrottle zero = new CountLogThrottle(0);
        assertThat(zero.tryAcquire()).isTrue();
        assertThat(zero.tryAcquire()).isTrue();

        CountLogThrottle negative = new CountLogThrottle(-1);
        assertThat(negative.tryAcquire()).isTrue();
        assertThat(negative.tryAcquire()).isTrue();
    }

    @Test
    public void countAllCalls() {
        CountLogThrottle throttle = new CountLogThrottle(10);

        throttle.tryAcquire();
        throttle.tryAcquire();
        throttle.tryAcquire();

        assertThat(throttle.getCounter()).isEqualTo(3);
    }
}
