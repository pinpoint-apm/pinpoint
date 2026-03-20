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

package com.navercorp.pinpoint.profiler.context.grpc.config;

import com.navercorp.pinpoint.common.config.Value;

/**
 * @author emeroad
 */
public class SpanBatchSenderConfig {

    private static final boolean DEFAULT_ENABLE = false;
    private static final int DEFAULT_SIZE = 50;
    private static final long DEFAULT_FLUSH_INTERVAL_MILLIS = 1000;
    private static final long DEFAULT_COLLECT_DEADLINE_TIME_MILLIS = 500;

    @Value("${profiler.transport.grpc.span.batch-sender.enable}")
    private boolean enable = DEFAULT_ENABLE;
    @Value("${profiler.transport.grpc.span.batch-sender.size}")
    private int size = DEFAULT_SIZE;
    @Value("${profiler.transport.grpc.span.batch-sender.flush.interval.millis}")
    private long flushIntervalMillis = DEFAULT_FLUSH_INTERVAL_MILLIS;
    @Value("${profiler.transport.grpc.span.batch-sender.collect.deadline.time.millis}")
    private long collectDeadlineTimeMillis = DEFAULT_COLLECT_DEADLINE_TIME_MILLIS;

    public boolean isEnable() {
        return enable;
    }

    public int getSize() {
        return size;
    }

    public long getFlushIntervalMillis() {
        return flushIntervalMillis;
    }

    public long getCollectDeadlineTimeMillis() {
        return collectDeadlineTimeMillis;
    }

    @Override
    public String toString() {
        return "SpanBatchSenderConfig{" +
                "enable=" + enable +
                ", size=" + size +
                ", flushIntervalMillis=" + flushIntervalMillis +
                ", collectDeadlineTimeMillis=" + collectDeadlineTimeMillis +
                '}';
    }
}