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
        double spread = 0.5; // 50% jitter
        Jitter jitter = new Jitter(spread);
        long baseDelay = 5000;
        
        // Test multiple times to ensure values are within expected range
        for (int i = 0; i < 100; i++) {
            long delay = jitter.nextDelay(baseDelay);
            
            // With 50% spread, delay should be in range [2500, 7500]
            // baseDelay +/- (baseDelay * 0.5)
            long minDelay = (long) (baseDelay * (1 - spread));
            long maxDelay = (long) (baseDelay * (1 + spread));
            
            assertThat(delay).isGreaterThanOrEqualTo(minDelay);
            assertThat(delay).isLessThanOrEqualTo(maxDelay);
        }
    }

    @Test
    public void testNextDelayWithZeroSpread() {
        Jitter jitter = new Jitter(0.0);
        long baseDelay = 5000;
        
        // With zero spread, delay should always equal base delay
        for (int i = 0; i < 10; i++) {
            long delay = jitter.nextDelay(baseDelay);
            assertThat(delay).isEqualTo(baseDelay);
        }
    }

    @Test
    public void testNextDelayWithSmallBaseDelay() {
        double spread = 0.5;
        Jitter jitter = new Jitter(spread);
        long baseDelay = 1; // Very small base delay
        
        long delay = jitter.nextDelay(baseDelay);
        
        // Should return base delay when spread time is 0
        assertThat(delay).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void testInvalidSpread() {
        assertThatThrownBy(() -> new Jitter(-0.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spread must be between 0 and 1.0");
        
        assertThatThrownBy(() -> new Jitter(1.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spread must be between 0 and 1.0");
    }

    @Test
    public void testDelayNeverNegative() {
        // Test edge case where jitter could potentially make delay negative
        double spread = 1.0; // 100% spread - maximum jitter
        Jitter jitter = new Jitter(spread);
        long baseDelay = 100;
        
        for (int i = 0; i < 100; i++) {
            long delay = jitter.nextDelay(baseDelay);
            assertThat(delay).isGreaterThanOrEqualTo(0);
        }
    }
}
