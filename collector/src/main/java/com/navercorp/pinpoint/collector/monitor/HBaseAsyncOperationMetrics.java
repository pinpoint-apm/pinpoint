package com.navercorp.pinpoint.collector.monitor;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.navercorp.pinpoint.common.hbase.HBaseAsyncOperation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Taejin Koo
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
        if (hBaseAsyncOperation == null) {
            throw new NullPointerException("null");
        }
        this.hBaseAsyncOperation = hBaseAsyncOperation;
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
