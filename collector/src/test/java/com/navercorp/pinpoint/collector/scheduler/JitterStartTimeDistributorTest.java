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

import com.google.common.collect.Range;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JitterStartTimeDistributorTest {
    @Test
     void nextTick() {
        JitterStartTimeDistributor distributor = new JitterStartTimeDistributor(1000, 0.0);
        long tick = distributor.nextTick();
        Assertions.assertTrue(tick > 0);
    }

    @Test
    void nextTick_jitter() {
        JitterStartTimeDistributor distributor = new JitterStartTimeDistributor(1000, 0.1);
        long tick = distributor.nextTick();
        Range<Long> range = Range.closed(900L, 1100L);

        Assertions.assertTrue(range.contains(tick));
        Assertions.assertTrue(range.contains(900L));
    }

}