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

import com.navercorp.pinpoint.common.profiler.clock.Clock;
import com.navercorp.pinpoint.common.profiler.clock.TickClock;
import org.apache.commons.math3.util.Precision;

import static com.navercorp.pinpoint.common.hbase.HbaseColumnFamily.AGENT_STAT_STATISTICS;

/**
 * @author HyunGil Jeong
 */
public class AgentStatUtils {

    public static final int NUM_DECIMALS = 4;
    public static final long CONVERT_VALUE = (long) Math.pow(10, NUM_DECIMALS);

    private static final TickClock CLOCK = new TickClock(Clock.systemUTC(), AGENT_STAT_STATISTICS.TIMESPAN_MS);

    public static long convertDoubleToLong(double value) {
        long convertedValue = (long) (value * CONVERT_VALUE);
        return convertedValue;
    }

    public static double convertLongToDouble(long value) {
        double convertedValue = ((double) value) / CONVERT_VALUE;
        return convertedValue;
    }

    public static double calculateRate(long count, long timeMs, int numDecimals, double defaultRate) {
        if (numDecimals < 0) {
            throw new IllegalArgumentException("numDecimals must be greater than 0");
        }
        if (timeMs < 1) {
            return defaultRate;
        }
        return Precision.round(count / (timeMs / 1000D), numDecimals);
    }

    public static long getBaseTimestamp(long timestamp) {
        return CLOCK.tick(timestamp);
    }
}
