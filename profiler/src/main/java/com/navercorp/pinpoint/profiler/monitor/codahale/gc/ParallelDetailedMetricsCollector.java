package com.navercorp.pinpoint.profiler.monitor.codahale.gc;

import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.EXCLUDED_DOUBLE;
import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_GC_PS_NEWGEN_COUNT;
import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_GC_PS_NEWGEN_TIME;
import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_PS_CODE_CACHE_USAGE;
import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_PS_METASPACE_USAGE;
import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_PS_NEWGEN_USAGE;
import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_PS_OLDGEN_USAGE;
import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_PS_PERMGEN_USAGE;
import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_PS_SURVIVOR_USAGE;
import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.getDoubleGauge;
import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.getLongGauge;

import java.util.SortedMap;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TJvmGcDetailed;

/**
 * HotSpot's Parallel (Old) collector with detailed metrics
 *
 * @author dawidmalina
 */
public class ParallelDetailedMetricsCollector extends ParallelCollector {

    private final Gauge<Double> codeCacheUsage;
    private final Gauge<Double> newGenUsage;
    private final Gauge<Double> oldGenUsage;
    private final Gauge<Double> survivorUsage;
    private final Gauge<Double> permGenUsage;
    private final Gauge<Double> metaspaceUsage;
    private final Gauge<Long> newGcCount;
    private final Gauge<Long> newGcTime;

    public ParallelDetailedMetricsCollector(MetricMonitorRegistry registry) {

        super(registry);

        final MetricRegistry metricRegistry = registry.getRegistry();
        @SuppressWarnings("rawtypes")
        final SortedMap<String, Gauge> gauges = metricRegistry.getGauges();

        this.codeCacheUsage = getDoubleGauge(gauges, JVM_MEMORY_POOLS_PS_CODE_CACHE_USAGE);
        this.newGenUsage = getDoubleGauge(gauges, JVM_MEMORY_POOLS_PS_NEWGEN_USAGE);
        this.oldGenUsage = getDoubleGauge(gauges, JVM_MEMORY_POOLS_PS_OLDGEN_USAGE);
        this.survivorUsage = getDoubleGauge(gauges, JVM_MEMORY_POOLS_PS_SURVIVOR_USAGE);

        if (gauges.containsKey(JVM_MEMORY_POOLS_PS_PERMGEN_USAGE)) {
            this.permGenUsage = getDoubleGauge(gauges, JVM_MEMORY_POOLS_PS_PERMGEN_USAGE);
            this.metaspaceUsage = EXCLUDED_DOUBLE;
        } else {
            this.metaspaceUsage = getDoubleGauge(gauges, JVM_MEMORY_POOLS_PS_METASPACE_USAGE);
            this.permGenUsage = EXCLUDED_DOUBLE;
        }

        this.newGcCount = getLongGauge(gauges, JVM_GC_PS_NEWGEN_COUNT);
        this.newGcTime = getLongGauge(gauges, JVM_GC_PS_NEWGEN_TIME);

    }

    @Override
    public TJvmGc collect() {
        final TJvmGc gc = super.collect();
        final TJvmGcDetailed details = new TJvmGcDetailed();
        details.setJvmPoolCodeCacheUsed(codeCacheUsage.getValue());
        details.setJvmPoolNewGenUsed(newGenUsage.getValue());
        details.setJvmPoolOldGenUsed(oldGenUsage.getValue());
        details.setJvmPoolSurvivorSpaceUsed(survivorUsage.getValue());
        if (EXCLUDED_DOUBLE.getValue() == permGenUsage.getValue()) {
            // metric for jvm >= 1.8
            details.setJvmPoolMetaspaceUsed(metaspaceUsage.getValue());
        } else {
            // metric for jvm < 1.8
            details.setJvmPoolPermGenUsed(permGenUsage.getValue());
        }
        details.setJvmGcNewCount(newGcCount.getValue());
        details.setJvmGcNewTime(newGcTime.getValue());
        gc.setJvmGcDetailed(details);
        return gc;
    }

    @Override
    public String toString() {
        return "HotSpot's Parallel (Old) collector with detailed metrics";
    }

}
