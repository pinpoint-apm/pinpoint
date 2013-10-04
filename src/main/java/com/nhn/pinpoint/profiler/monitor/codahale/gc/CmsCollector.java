package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_GC_CMS_COUNT;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_GC_CMS_TIME;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_GC_PARNEW_COUNT;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_GC_PARNEW_TIME;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_HEAP_MAX;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_HEAP_USED;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_NONHEAP_MAX;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_NONHEAP_USED;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_CMS_OLDGEN;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_CMS_PERMGEN;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_CODECACHE;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_PAREDEN;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_POOLS_PARSURVIVOR;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_TOTAL_MAX;
import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.JVM_MEMORY_TOTAL_USED;

import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TStatWithCmsCollector;

/**
 * HotSpot's Concurrent-Mark-Sweep collector
 * 
 * @author harebox
 */
public class CmsCollector extends GarbageCollectorType {

	@Override
	public int getTypeCode() {
		return GarbageCollectorType.CMS_COLLECTOR;
	}

	@Override
	public void map(MetricMonitorRegistry registry, TAgentStat agentStat, Object typeObject, String agentId) {
		MetricRegistry r = registry.getRegistry();
		TStatWithCmsCollector stat = (TStatWithCmsCollector) typeObject;
		if (stat == null) {
			stat = new TStatWithCmsCollector();
			agentStat.setCms(stat);
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
		stat.setJvmMemoryPoolsParEdenSpaceUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_PAREDEN));
		stat.setJvmMemoryPoolsParSurvivorSpaceUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_PARSURVIVOR));
		stat.setJvmMemoryPoolsCMSOldGenUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_CMS_OLDGEN));
		stat.setJvmMemoryPoolsCMSPermGenUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_CMS_PERMGEN));
		stat.setJvmGcCmsCount(MetricMonitorValues.getLong(r, JVM_GC_CMS_COUNT));
		stat.setJvmGcCmsTime(MetricMonitorValues.getLong(r, JVM_GC_CMS_TIME));
		stat.setJvmGcParNewCount(MetricMonitorValues.getLong(r, JVM_GC_PARNEW_COUNT));
		stat.setJvmGcParNewTime(MetricMonitorValues.getLong(r, JVM_GC_PARNEW_TIME));
	}

	@Override
	public String toString() {
		return "HotSpot's Concurrent-Mark-Sweep collector";
	}
	
}
