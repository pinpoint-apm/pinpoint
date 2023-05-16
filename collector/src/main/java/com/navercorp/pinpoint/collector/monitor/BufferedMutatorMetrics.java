package com.navercorp.pinpoint.collector.monitor;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.navercorp.pinpoint.common.hbase.batch.BufferedMutatorWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BufferedMutatorMetrics implements MetricSet {
    private static final String HBASE_ASYNC_OPS = "hbase.bufferedmutator.ops";

    private static final String COUNT = HBASE_ASYNC_OPS + ".success.count";
    private static final String REJECTED_COUNT = HBASE_ASYNC_OPS + ".rejected.count";

    private final BufferedMutatorWriter writer;

    public BufferedMutatorMetrics(BufferedMutatorWriter writer) {
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    @Override
    public Map<String, Metric> getMetrics() {

        final Map<String, Metric> gauges = new HashMap<>(3);
        gauges.put(COUNT, new Gauge<Long>() {
            @Override
            public Long getValue() {
                return writer.getSuccessCount();
            }
        });
        gauges.put(REJECTED_COUNT, new Gauge<Long>() {
            @Override
            public Long getValue() {
                return writer.getErrorCount();
            }
        });

        return gauges;
    }
}
