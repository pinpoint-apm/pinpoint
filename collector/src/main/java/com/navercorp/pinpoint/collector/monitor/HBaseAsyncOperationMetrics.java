/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.monitor;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.navercorp.pinpoint.common.hbase.HBaseAsyncOperation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class HBaseAsyncOperationMetrics implements MetricSet {

    private static final String HBASE_ASYNC_OPS = "hbase.async.ops";
    private static final String COUNT = HBASE_ASYNC_OPS + ".count";
    private static final String REJECTED_COUNT = HBASE_ASYNC_OPS + ".rejected.count";
    private static final String FAILED_COUNT = HBASE_ASYNC_OPS + ".failed.count";
    private static final String WAITING_COUNT = HBASE_ASYNC_OPS + ".waiting.count";
    private static final String AVERAGE_LATENCY = HBASE_ASYNC_OPS + ".latency.value";

    private final HBaseAsyncOperation hBaseAsyncOperation;

    public HBaseAsyncOperationMetrics(HBaseAsyncOperation hBaseAsyncOperation) {
        this.hBaseAsyncOperation = Objects.requireNonNull(hBaseAsyncOperation, "hBaseAsyncOperation");
    }

    @Override
    public Map<String, Metric> getMetrics() {
        if (!hBaseAsyncOperation.isAvailable()) {
            return Collections.emptyMap();
        }

        final Map<String, Metric> gauges = new HashMap<>(3);
        gauges.put(COUNT, new Gauge<Long>() {
            @Override
            public Long getValue() {
                return hBaseAsyncOperation.getOpsCount();
            }
        });
        gauges.put(REJECTED_COUNT, new Gauge<Long>() {
            @Override
            public Long getValue() {
                return hBaseAsyncOperation.getOpsRejectedCount();
            }
        });
        gauges.put(FAILED_COUNT, new Gauge<Long>() {
            @Override
            public Long getValue() {
                return hBaseAsyncOperation.getOpsFailedCount();
            }
        });
        gauges.put(WAITING_COUNT, new Gauge<Long>() {
            @Override
            public Long getValue() {
                return hBaseAsyncOperation.getCurrentOpsCount();
            }
        });
        gauges.put(AVERAGE_LATENCY, new Gauge<Long>() {
            @Override
            public Long getValue() {
                return hBaseAsyncOperation.getOpsAverageLatency();
            }
        });

        return Collections.unmodifiableMap(gauges);
    }

}
