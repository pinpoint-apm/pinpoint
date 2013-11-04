package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import java.util.Collection;

import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TJvmGcType;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

/**
 * @author harebox
 */
public abstract class GarbageCollectorType {

	abstract public int getTypeCode();

	abstract public void map(MetricMonitorRegistry registry, TAgentStat agentStat, String agentId);
	
	/**
	 * 타입 코드로 생성
	 */
	public static GarbageCollectorType newType(int type) {
		if (type == SERIAL_COLLECTOR) {
			return new SerialCollector();
		} else if (type == PARALLEL_COLLECTOR) {
			return new ParallelCollector();
		} else if (type == CMS_COLLECTOR) {
			return new CmsCollector();
		} else if (type == G1_COLLECTOR) {
			return new G1Collector();
		} else {
			throw new IllegalArgumentException("incorrect garbage collector code");
		}
	}
	
	/**
	 * 통계 키를 기반으로 생성
	 */
	public static GarbageCollectorType newType(MetricMonitorRegistry registry) {
		Collection<String> keys = registry.getRegistry().getNames();
		if (keys.contains(JVM_GC_SERIAL_MSC_COUNT)) {
			return new SerialCollector();
		} else if (keys.contains(JVM_GC_PS_MS_COUNT)) {
			return new ParallelCollector();
		} else if (keys.contains(JVM_GC_CMS_COUNT)) {
			return new CmsCollector();
		} else if (keys.contains(JVM_GC_G1_OLD_COUNT)) {
			return new G1Collector();
		} else {
			throw new RuntimeException("unknown garbage collector");
		}
	}

	// FIXME AgentStat 자체를 타입으로 써도 되지만 일단 이렇게 해둔다.
	public static final int SERIAL_COLLECTOR = TJvmGcType.SERIAL.ordinal();
	public static final int PARALLEL_COLLECTOR = TJvmGcType.PARALLEL.ordinal();
	public static final int CMS_COLLECTOR = TJvmGcType.CMS.ordinal();
	public static final int G1_COLLECTOR = TJvmGcType.G1.ordinal();

}
