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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author emeroad
 */
public class JitterTest {

    @Test
    public void testNextDelay() {
        long maxJitter = 5000;
        Jitter jitter = new Jitter(maxJitter);
        
        long delay = jitter.nextDelay();
        
        // Delay should be between 0 (inclusive) and maxJitter (exclusive)
        assertThat(delay).isGreaterThanOrEqualTo(0L);
        assertThat(delay).isLessThan(maxJitter);
    }

    @Test
    public void testNextDelayMultipleCalls() {
        long maxJitter = 5000;
        Jitter jitter = new Jitter(maxJitter);
        
        // Test that multiple calls produce values in valid range
        for (int i = 0; i < 100; i++) {
            long delay = jitter.nextDelay();
            assertThat(delay).isGreaterThanOrEqualTo(0L);
            assertThat(delay).isLessThan(maxJitter);
        }
    }

    @Test
    public void testInvalidMaxJitter() {
        assertThatThrownBy(() -> new Jitter(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxJitter must be positive");
        
        assertThatThrownBy(() -> new Jitter(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxJitter must be positive");
    }
}
