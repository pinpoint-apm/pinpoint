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
package com.navercorp.pinpoint.collector.monitor.micrometer;

import com.navercorp.pinpoint.common.hbase.counter.HBaseBatchPerformance;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
public class HBaseAsyncOperationMetrics {

    private static final String HBASE_ASYNC_OPS = "hbase.async.ops";
    private static final String COUNT = HBASE_ASYNC_OPS + ".count";
    private static final String REJECTED_COUNT = HBASE_ASYNC_OPS + ".rejected.count";
    private static final String FAILED_COUNT = HBASE_ASYNC_OPS + ".failed.count";
    private static final String WAITING_COUNT = HBASE_ASYNC_OPS + ".waiting.count";
    private static final String AVERAGE_LATENCY = HBASE_ASYNC_OPS + ".latency.value";

    private final List<HBaseBatchPerformance> hBaseAsyncOperations;
    private final MeterRegistry meterRegistry;

    public HBaseAsyncOperationMetrics(List<HBaseBatchPerformance> hBaseAsyncOperationList, MeterRegistry meterRegistry) {
        this.hBaseAsyncOperations = Objects.requireNonNull(hBaseAsyncOperationList, "hBaseAsyncOperationList").stream()
                .filter(HBaseBatchPerformance::isAvailable)
                .collect(Collectors.toList());
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry");
        registerMetrics();
    }

    private void registerMetrics() {
        if (CollectionUtils.isEmpty(hBaseAsyncOperations)) {
            return;
        }

        Gauge.builder(COUNT, hBaseAsyncOperations, ops -> ops.stream()
                        .mapToLong(HBaseBatchPerformance::getOpsCount)
                        .sum())
                .register(meterRegistry);

        Gauge.builder(REJECTED_COUNT, hBaseAsyncOperations, ops -> ops.stream()
                        .mapToLong(HBaseBatchPerformance::getOpsRejectedCount)
                        .sum())
                .register(meterRegistry);

        Gauge.builder(FAILED_COUNT, hBaseAsyncOperations, ops -> ops.stream()
                        .mapToLong(HBaseBatchPerformance::getOpsFailedCount)
                        .sum())
                .register(meterRegistry);

        Gauge.builder(WAITING_COUNT, hBaseAsyncOperations, ops -> ops.stream()
                        .mapToLong(HBaseBatchPerformance::getCurrentOpsCount)
                        .sum())
                .register(meterRegistry);
    }

}
