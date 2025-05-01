/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.applicationmap.redis.statistics;

import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.TimeSeriesKey;
import com.navercorp.pinpoint.collector.applicationmap.redis.schema.TimeSeriesValue;
import com.navercorp.pinpoint.redis.timeseries.RedisTimeseriesAsyncCommands;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class RedisBulkWriter implements BulkWriter<TimeSeriesKey, TimeSeriesValue> {

    RedisTimeseriesAsyncCommands commands;

    public RedisBulkWriter(RedisTimeseriesAsyncCommands commands) {
        this.commands = Objects.requireNonNull(commands, "commands");
    }

    @Override
    public void increment(TimeSeriesKey timeSeriesKey, TimeSeriesValue timeSeriesValue) {
        commands.tsAdd(timeSeriesKey.getKey(), timeSeriesValue.getTimestamp(), 1);
    }

    @Override
    public void increment(TimeSeriesKey timeSeriesKey, TimeSeriesValue timeSeriesValue, long addition) {
        commands.tsAdd(timeSeriesKey.getKey(), timeSeriesValue.getTimestamp(), addition);
    }

    @Override
    public void updateMax(TimeSeriesKey timeSeriesKey, TimeSeriesValue timeSeriesValue, long value) {
        // calculate max later
        commands.tsAdd(timeSeriesKey.getKey(), timeSeriesValue.getTimestamp(), value);
    }

    @Override
    public void flushLink() {
        // do nothing
    }

    @Override
    public void flushAvgMax() {
        // do nothing
    }
}
