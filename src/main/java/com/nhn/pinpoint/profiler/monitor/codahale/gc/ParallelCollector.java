package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TStatWithParallelCollector;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

/**
 * HotSpot's Parallel (Old) collector
 * 
 * @author harebox
 */
public class ParallelCollector extends GarbageCollectorType {

	@Override
	public int getTypeCode() {
		return GarbageCollectorType.PARALLEL_COLLECTOR;
	}

	@Override
	public void map(MetricMonitorRegistry registry, TAgentStat agentStat, Object typeObject, String agentId) {
		MetricRegistry r = registry.getRegistry();
		TStatWithParallelCollector stat = (TStatWithParallelCollector) typeObject;
		if (stat == null) {
			stat = new TStatWithParallelCollector();
			agentStat.setParallel(stat);
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
		stat.setJvmMemoryPoolsPSEdenSpaceUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_PS_EDEN));
		stat.setJvmMemoryPoolsPSSurvivorSpaceUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_PS_SURVIVOR));
		stat.setJvmMemoryPoolsPSOldGenUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_PS_OLDGEN));
		stat.setJvmMemoryPoolsPSPermGenUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_PS_PERMGEN));
		stat.setJvmGcPSMarkSweepCount(MetricMonitorValues.getLong(r, JVM_GC_PS_MS_COUNT));
		stat.setJvmGcPSMarkSweepTime(MetricMonitorValues.getLong(r, JVM_GC_PS_MS_TIME));
		stat.setJvmGcPSScavengeCount(MetricMonitorValues.getLong(r, JVM_GC_PS_SCAVENGE_COUNT));
		stat.setJvmGcPSMarkSweepTime(MetricMonitorValues.getLong(r, JVM_GC_PS_SCAVENGE_TIME));
	}

	@Override
	public String toString() {
		return "HotSpot's Parallel (Old) collector";
	}
	
}
