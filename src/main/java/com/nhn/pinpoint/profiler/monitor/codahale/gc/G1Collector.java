package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TStatWithG1Collector;

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
	public void map(MetricMonitorRegistry registry, TAgentStat agentStat, Object typeObject, String agentId) {
		MetricRegistry r = registry.getRegistry();
		TStatWithG1Collector stat = (TStatWithG1Collector) typeObject;
		if (stat == null) {
			stat = new TStatWithG1Collector();
			agentStat.setG1(stat);
		}
		stat.setAgentId(agentId);
		stat.setTimestamp(System.currentTimeMillis());
		stat.setJvmMemoryTotalMax(MetricMonitorValues.getLong(r, JVM_MEMORY_TOTAL_MAX));
		stat.setJvmMemoryTotalUsed(MetricMonitorValues.getLong(r, JVM_MEMORY_TOTAL_USED));
		stat.setJvmMemoryHeapMax(MetricMonitorValues.getLong(r, JVM_MEMORY_HEAP_MAX));
		stat.setJvmMemoryHeapUsed(MetricMonitorValues.getLong(r, JVM_MEMORY_HEAP_USED));
		stat.setJvmMemoryNonHeapMax(MetricMonitorValues.getLong(r, JVM_MEMORY_NONHEAP_MAX));
		stat.setJvmMemoryNonHeapUsed(MetricMonitorValues.getLong(r, JVM_MEMORY_NONHEAP_USED));
		stat.setJvmMemoryPoolsCodeCacheUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_CODECACHE));
		stat.setJvmMemoryPoolsG1EdenSpaceUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_G1_EDEN));
		stat.setJvmMemoryPoolsG1SurvivorSpaceUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_G1_SURVIVOR));
		stat.setJvmMemoryPoolsG1OldGenUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_G1_OLDGEN));
		stat.setJvmMemoryPoolsG1PermGenUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_G1_PERMGEN));
		stat.setJvmGcG1OldGenerationCount(MetricMonitorValues.getLong(r, JVM_GC_G1_OLD_COUNT));
		stat.setJvmGcG1OldGenerationTime(MetricMonitorValues.getLong(r, JVM_GC_G1_OLD_TIME));
		stat.setJvmGcG1YoungGenerationCount(MetricMonitorValues.getLong(r, JVM_GC_G1_YOUNG_COUNT));
		stat.setJvmGcG1YoungGenerationTime(MetricMonitorValues.getLong(r, JVM_GC_G1_YOUNG_TIME));
	}

	@Override
	public String toString() {
		return "HotSpot's Garbage-First(G1) collector";
	}
	
}
