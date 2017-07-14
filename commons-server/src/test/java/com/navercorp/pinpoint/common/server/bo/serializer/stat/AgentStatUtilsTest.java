/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.AGENT_STAT_TIMESPAN_MS;

/**
 * @author HyunGil Jeong
 */
public class AgentStatUtilsTest {

    private static final Random RANDOM = new Random();

    private static final double DECIMAL_COMPARISON_DELTA = 1 / Math.pow(10, AgentStatUtils.NUM_DECIMALS);

    @Test
    public void testConversion() {
        double originalValue = Math.random();
        double convertedValue = AgentStatUtils.convertLongToDouble(AgentStatUtils.convertDoubleToLong(originalValue));
        Assert.assertEquals(originalValue, convertedValue, DECIMAL_COMPARISON_DELTA);

        originalValue = 0;
        convertedValue = AgentStatUtils.convertLongToDouble(AgentStatUtils.convertDoubleToLong(originalValue));
        Assert.assertEquals(originalValue, convertedValue, DECIMAL_COMPARISON_DELTA);

        originalValue = -1 * Math.random();
        convertedValue = AgentStatUtils.convertLongToDouble(AgentStatUtils.convertDoubleToLong(originalValue));
        Assert.assertEquals(originalValue, convertedValue, DECIMAL_COMPARISON_DELTA);
    }

    @Test
    public void calculateRate_should_return_defaultRate_if_time_is_not_greater_than_0() {
        long count = 1000;
        int numDecimals = 2;
        double defaultRate = 99;
        double validDelta = 1 / Math.pow(10, numDecimals);

        long timeMs = 0;
        double rate = AgentStatUtils.calculateRate(count, timeMs, numDecimals, defaultRate);
        Assert.assertEquals(defaultRate, rate, validDelta);

        timeMs = -1;
        rate = AgentStatUtils.calculateRate(count, timeMs, numDecimals, defaultRate);
        Assert.assertEquals(defaultRate, rate, validDelta);
    }

    @Test
    public void calculateRate_should_return_correct_rate_to_numDecimal_places() {
        long count = 1000;
        long timeMs = 1000;
        int numDecimals = 0;
        double defaultRate = 0;
        double expectedRate = 1000;

        double rate = AgentStatUtils.calculateRate(count, timeMs, numDecimals, defaultRate);
        Assert.assertEquals(Double.doubleToLongBits(expectedRate), Double.doubleToLongBits(rate));
    }

    @Test
    public void getBaseTimestamp_should_return_a_multiple_of_AGENT_STAT_TIMESPAN_MS() {
        long timestamp = RANDOM.nextLong();
        long baseTimestamp = AgentStatUtils.getBaseTimestamp(timestamp);
        Assert.assertTrue((baseTimestamp % AGENT_STAT_TIMESPAN_MS) == 0);

    }
}
