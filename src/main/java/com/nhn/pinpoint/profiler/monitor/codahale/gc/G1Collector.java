package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;

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
		MetricRegistry r = registry.getRegistry();
		TJvmGc gc = agentStat.getGc();
		if (gc == null) {
			gc = new TJvmGc();
			agentStat.setGc(gc);
		}
		gc.setType(TJvmGcType.G1);
		gc.setJvmMemoryHeapMax(MetricMonitorValues.getLong(r, JVM_MEMORY_HEAP_MAX));
		gc.setJvmMemoryHeapUsed(MetricMonitorValues.getLong(r, JVM_MEMORY_HEAP_USED));
		gc.setJvmMemoryNonHeapMax(MetricMonitorValues.getLong(r, JVM_MEMORY_NONHEAP_MAX));
		gc.setJvmMemoryNonHeapUsed(MetricMonitorValues.getLong(r, JVM_MEMORY_NONHEAP_USED));
		gc.setJvmGcOldCount(MetricMonitorValues.getLong(r, JVM_GC_G1_OLD_COUNT));
		gc.setJvmGcOldTime(MetricMonitorValues.getLong(r, JVM_GC_G1_OLD_TIME));
	}

	@Override
	public String toString() {
		return "HotSpot's Garbage-First(G1) collector";
	}
	
}
