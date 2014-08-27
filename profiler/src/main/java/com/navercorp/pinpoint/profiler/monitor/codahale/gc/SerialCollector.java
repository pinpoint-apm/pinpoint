package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;

import java.util.SortedMap;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

/**
 * HotSpot's Serial collector
 * @author emeroad
 * @author harebox
 */
public class SerialCollector implements GarbageCollector {

    public static final TJvmGcType GC_TYPE = TJvmGcType.SERIAL;

    private final Gauge<Long> heapMax;
    private final Gauge<Long> heapUsed;

    private final Gauge<Long> heapNonHeapMax;
    private final Gauge<Long> heapNonHeapUsed;

    private final Gauge<Long> gcCount;
    private final Gauge<Long> gcTime;

    public SerialCollector(MetricMonitorRegistry registry) {
        if (registry == null) {
            throw new NullPointerException("registry must not be null");
        }

        final MetricRegistry metricRegistry = registry.getRegistry();
        final SortedMap<String, Gauge> gauges = metricRegistry.getGauges();

        this.heapMax = MetricMonitorValues.getLongGauge(gauges, JVM_MEMORY_HEAP_MAX);
        this.heapUsed = MetricMonitorValues.getLongGauge(gauges, JVM_MEMORY_HEAP_USED);

        this.heapNonHeapMax = MetricMonitorValues.getLongGauge(gauges, JVM_MEMORY_NONHEAP_MAX);
        this.heapNonHeapUsed = MetricMonitorValues.getLongGauge(gauges, JVM_MEMORY_NONHEAP_USED);

        this.gcCount = MetricMonitorValues.getLongGauge(gauges, JVM_GC_SERIAL_MSC_COUNT);
        this.gcTime = MetricMonitorValues.getLongGauge(gauges, JVM_GC_SERIAL_MSC_TIME);
    }

    @Override
	public int getTypeCode() {
		return GC_TYPE.ordinal();
	}

	@Override
	public TJvmGc collect() {

        final TJvmGc gc = new TJvmGc();
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
		return "HotSpot's Serial collector";
	}
	
}
