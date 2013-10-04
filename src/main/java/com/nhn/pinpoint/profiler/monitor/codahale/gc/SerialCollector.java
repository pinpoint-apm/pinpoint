package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TStatWithSerialCollector;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

/**
 * HotSpot's Serial collector
 * 
 * @author harebox
 */
public class SerialCollector extends GarbageCollectorType {

	@Override
	public int getTypeCode() {
		return GarbageCollectorType.SERIAL_COLLECTOR;
	}

	@Override
	public void map(MetricMonitorRegistry registry, TAgentStat agentStat, Object typeObject, String agentId) {
		MetricRegistry r = registry.getRegistry();
		TStatWithSerialCollector stat = (TStatWithSerialCollector) typeObject;
		if (stat == null) {
			stat = new TStatWithSerialCollector();
			agentStat.setSerial(stat);
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
		stat.setJvmMemoryPoolsEdenSpaceUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_EDEN));
		stat.setJvmMemoryPoolsPermGenUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_PERMGEN));
		stat.setJvmMemoryPoolsSurvivorSpaceUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_SURVIVOR));
		stat.setJvmMemoryPoolsTenuredGenUsage(MetricMonitorValues.getLong(r, JVM_MEMORY_POOLS_TENURED));
		stat.setJvmGcCopyCount(MetricMonitorValues.getLong(r, JVM_GC_SERIAL_COPY_COUNT));
		stat.setJvmGcCopyTime(MetricMonitorValues.getLong(r, JVM_GC_SERIAL_COPY_TIME));
		stat.setJvmGcMarkSweepCompactCount(MetricMonitorValues.getLong(r, JVM_GC_SERIAL_MSC_COUNT));
		stat.setJvmGcMarkSweepCompactTime(MetricMonitorValues.getLong(r, JVM_GC_SERIAL_MSC_TIME));
	}

	@Override
	public String toString() {
		return "HotSpot's Serial collector";
	}
	
}
