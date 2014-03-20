package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;

import java.util.SortedMap;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

/**
 * HotSpot's Garbage-First(G1) collector
 * 
 * @author harebox
 */
public class G1Collector extends GarbageCollectorType {

	@Override
	public int getTypeCode() {
		return GarbageCollectorType.G1_COLLECTOR;
	}

	@Override
	public void map(MetricMonitorRegistry registry, TAgentStat agentStat, String agentId) {
		final MetricRegistry metricRegistry = registry.getRegistry();
		TJvmGc gc = agentStat.getGc();
		if (gc == null) {
			gc = new TJvmGc();
			agentStat.setGc(gc);
		}
		gc.setType(TJvmGcType.G1);

        final SortedMap<String, Gauge> gauges = metricRegistry.getGauges();
		gc.setJvmMemoryHeapMax(MetricMonitorValues.getLong(gauges, JVM_MEMORY_HEAP_MAX));
		gc.setJvmMemoryHeapUsed(MetricMonitorValues.getLong(gauges, JVM_MEMORY_HEAP_USED));
		gc.setJvmMemoryNonHeapMax(MetricMonitorValues.getLong(gauges, JVM_MEMORY_NONHEAP_MAX));
		gc.setJvmMemoryNonHeapUsed(MetricMonitorValues.getLong(gauges, JVM_MEMORY_NONHEAP_USED));
		gc.setJvmGcOldCount(MetricMonitorValues.getLong(gauges, JVM_GC_G1_OLD_COUNT));
		gc.setJvmGcOldTime(MetricMonitorValues.getLong(gauges, JVM_GC_G1_OLD_TIME));
	}

	@Override
	public String toString() {
		return "HotSpot's Garbage-First(G1) collector";
	}
	
}
