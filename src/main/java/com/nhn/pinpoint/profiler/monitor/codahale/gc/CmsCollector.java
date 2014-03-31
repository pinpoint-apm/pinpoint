package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_GC_CMS_COUNT;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_GC_CMS_TIME;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_HEAP_MAX;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_HEAP_USED;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_NONHEAP_MAX;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_NONHEAP_USED;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;

import java.util.SortedMap;

/**
 * HotSpot's Concurrent-Mark-Sweep collector
 *
 * @author emeroad
 * @author harebox
 */
public class CmsCollector implements GarbageCollector {


    public static final TJvmGcType GC_TYPE = TJvmGcType.CMS;

    private final Gauge<Long> heapMax;
    private final Gauge<Long> heapUsed;

    private final Gauge<Long> heapNonHeapMax;
    private final Gauge<Long> heapNonHeapUsed;

    private final Gauge<Long> gcCount;
    private final Gauge<Long> gcTime;

    public CmsCollector(MetricMonitorRegistry registry) {
        if (registry == null) {
            throw new NullPointerException("registry must not be null");
        }

        final MetricRegistry metricRegistry = registry.getRegistry();
        final SortedMap<String, Gauge> gauges = metricRegistry.getGauges();

        this.heapMax = MetricMonitorValues.getLongGauge(gauges, JVM_MEMORY_HEAP_MAX);
        this.heapUsed = MetricMonitorValues.getLongGauge(gauges, JVM_MEMORY_HEAP_USED);

        this.heapNonHeapMax = MetricMonitorValues.getLongGauge(gauges, JVM_MEMORY_NONHEAP_MAX);
        this.heapNonHeapUsed = MetricMonitorValues.getLongGauge(gauges, JVM_MEMORY_NONHEAP_USED);

        this.gcCount = MetricMonitorValues.getLongGauge(gauges, JVM_GC_CMS_COUNT);
        this.gcTime = MetricMonitorValues.getLongGauge(gauges, JVM_GC_CMS_TIME);

    }

    @Override
	public int getTypeCode() {
		return GC_TYPE.ordinal();
	}

	@Override
	public TJvmGc collect() {

        TJvmGc gc = new TJvmGc();
        gc.setType(GC_TYPE);
        gc.setJvmMemoryHeapMax(heapMax.getValue());
        gc.setJvmMemoryHeapUsed(heapUsed.getValue());

        gc.setJvmMemoryNonHeapMax(heapNonHeapMax.getValue());
        gc.setJvmMemoryNonHeapUsed(heapNonHeapUsed.getValue());

		gc.setJvmGcOldCount(gcCount.getValue());
		gc.setJvmGcOldTime(gcTime.getValue());

        return gc;
	}

	@Override
	public String toString() {
		return "HotSpot's Concurrent-Mark-Sweep collector";
	}
	
}
