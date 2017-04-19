/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.response;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * @author Taejin Koo
 */
public class ReuseResponseTimeCollectorTest {

    private final Random random = new Random(System.currentTimeMillis());

    @Test
    public void defaultTest() throws Exception {

        int count = 3;

        ReuseResponseTimeCollector reuseResponseTimeCollector = new ReuseResponseTimeCollector();

        long totalValue = 0;
        for (int i = 0; i < count; i++) {
            long value = Math.max(500, (random.nextLong() % 2500) + 500);
            totalValue += value;
            reuseResponseTimeCollector.add(value);
        }

        ResponseTimeValue responseTimeValue = reuseResponseTimeCollector.resetAndGetValue();
        Assert.assertEquals(totalValue / count, responseTimeValue.getAvg());

        responseTimeValue = reuseResponseTimeCollector.resetAndGetValue();
        Assert.assertEquals(0, responseTimeValue.getAvg());
    }

}
