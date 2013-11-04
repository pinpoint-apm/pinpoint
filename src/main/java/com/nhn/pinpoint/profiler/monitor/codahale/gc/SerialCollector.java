package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;

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
	public void map(MetricMonitorRegistry registry, TAgentStat agentStat, String agentId) {
		MetricRegistry r = registry.getRegistry();
		TJvmGc gc = agentStat.getGc();
		if (gc == null) {
			gc = new TJvmGc();
			agentStat.setGc(gc);
		}
		gc.setType(TJvmGcType.SERIAL);
		gc.setJvmMemoryHeapMax(MetricMonitorValues.getLong(r, JVM_MEMORY_HEAP_MAX));
		gc.setJvmMemoryHeapUsed(MetricMonitorValues.getLong(r, JVM_MEMORY_HEAP_USED));
		gc.setJvmMemoryNonHeapMax(MetricMonitorValues.getLong(r, JVM_MEMORY_NONHEAP_MAX));
		gc.setJvmMemoryNonHeapUsed(MetricMonitorValues.getLong(r, JVM_MEMORY_NONHEAP_USED));
		gc.setJvmGcOldCount(MetricMonitorValues.getLong(r, JVM_GC_SERIAL_MSC_COUNT));
		gc.setJvmGcOldTime(MetricMonitorValues.getLong(r, JVM_GC_SERIAL_MSC_TIME));
	}

	@Override
	public String toString() {
		return "HotSpot's Serial collector";
	}
	
}
