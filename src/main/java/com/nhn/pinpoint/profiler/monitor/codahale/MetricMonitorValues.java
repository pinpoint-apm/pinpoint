package com.nhn.pinpoint.profiler.monitor.codahale;

import java.util.SortedMap;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;


/**
 * @author harebox
 */
public class MetricMonitorValues {

	public static final String JVM_GC = "jvm.gc";
	// Serial collector
	public static final String JVM_GC_SERIAL_COPY_COUNT = JVM_GC + ".Copy.count";
	public static final String JVM_GC_SERIAL_COPY_TIME = JVM_GC + ".Copy.time";
	public static final String JVM_GC_SERIAL_MSC_COUNT = JVM_GC + ".MarkSweepCompact.count";
	public static final String JVM_GC_SERIAL_MSC_TIME = JVM_GC + ".MarkSweepCompact.time";
	// Parallel (Old) collector
	public static final String JVM_GC_PS_MS_COUNT = JVM_GC + ".PS-MarkSweep.count";
	public static final String JVM_GC_PS_MS_TIME = JVM_GC + ".PS-MarkSweep.time";
	public static final String JVM_GC_PS_SCAVENGE_COUNT = JVM_GC + ".PS-Scavenge.count";
	public static final String JVM_GC_PS_SCAVENGE_TIME = JVM_GC + ".PS-Scavenge.time";
	// CMS collector
	public static final String JVM_GC_CMS_COUNT = JVM_GC + ".ConcurrentMarkSweep.count";
	public static final String JVM_GC_CMS_TIME = JVM_GC + ".ConcurrentMarkSweep.time";
	public static final String JVM_GC_PARNEW_COUNT = JVM_GC + ".ParNew.count";
	public static final String JVM_GC_PARNEW_TIME = JVM_GC + ".ParNew.time";
	// G1 collector
	public static final String JVM_GC_G1_OLD_COUNT = JVM_GC + ".G1-Old-Generation.count";
	public static final String JVM_GC_G1_OLD_TIME = JVM_GC + ".G1-Old-Generation.time";
	public static final String JVM_GC_G1_YOUNG_COUNT = JVM_GC + ".G1-Young-Generation.count";
	public static final String JVM_GC_G1_YOUNG_TIME = JVM_GC + ".G1-Young-Generation.time";

	
	public static final String JVM_MEMORY = "jvm.memory";
	// commons
	public static final String JVM_MEMORY_HEAP_INIT = JVM_MEMORY + ".heap.init";
	public static final String JVM_MEMORY_HEAP_USED = JVM_MEMORY + ".heap.used";
	public static final String JVM_MEMORY_HEAP_COMMITTED = JVM_MEMORY + ".heap.committed";
	public static final String JVM_MEMORY_HEAP_MAX = JVM_MEMORY + ".heap.max";
	public static final String JVM_MEMORY_NONHEAP_INIT = JVM_MEMORY + ".non-heap.init";
	public static final String JVM_MEMORY_NONHEAP_USED = JVM_MEMORY + ".non-heap.used";
	public static final String JVM_MEMORY_NONHEAP_COMMITTED = JVM_MEMORY + ".non-heap.committed";
	public static final String JVM_MEMORY_NONHEAP_MAX = JVM_MEMORY + ".non-heap.max";
	public static final String JVM_MEMORY_TOTAL_INIT = JVM_MEMORY + ".total.init";
	public static final String JVM_MEMORY_TOTAL_USED = JVM_MEMORY + ".total.used";
	public static final String JVM_MEMORY_TOTAL_COMMITTED = JVM_MEMORY + ".total.committed";
	public static final String JVM_MEMORY_TOTAL_MAX = JVM_MEMORY + ".total.max";
	// Serial collector
	public static final String JVM_MEMORY_POOLS_EDEN = JVM_MEMORY + ".pools.Eden-Space.usage";
	public static final String JVM_MEMORY_POOLS_PERMGEN = JVM_MEMORY + ".pools.Perm-Gen.usage";
	public static final String JVM_MEMORY_POOLS_SURVIVOR = JVM_MEMORY + ".pools.Survivor-Space.usage";
	public static final String JVM_MEMORY_POOLS_TENURED = JVM_MEMORY + ".pools.Tenured-Gen.usage";
	// Parallel (Old) collector
	public static final String JVM_MEMORY_POOLS_PS_EDEN = JVM_MEMORY + ".pools.PS-Eden-Space.usage";
	public static final String JVM_MEMORY_POOLS_PS_OLDGEN = JVM_MEMORY + ".pools.PS-Old-Gen.usage";
	public static final String JVM_MEMORY_POOLS_PS_PERMGEN = JVM_MEMORY + ".pools.PS-Perm-Gen.usage";
	public static final String JVM_MEMORY_POOLS_PS_SURVIVOR = JVM_MEMORY + ".pools.PS-Survivor-Space.usage";
	// CMS collector
	public static final String JVM_MEMORY_POOLS_CMS_OLDGEN = JVM_MEMORY + ".pools.CMS-Old-Gen.usage";
	public static final String JVM_MEMORY_POOLS_CMS_PERMGEN = JVM_MEMORY + ".pools.CMS-Perm-Gen.usage";
	public static final String JVM_MEMORY_POOLS_CODECACHE = JVM_MEMORY + ".pools.Code-Cache.usage";
	public static final String JVM_MEMORY_POOLS_PAREDEN = JVM_MEMORY + ".pools.Par-Eden-Space.usage";
	public static final String JVM_MEMORY_POOLS_PARSURVIVOR = JVM_MEMORY + ".pools.Par-Survivor-Space.usage";
	// G1 collector
	public static final String JVM_MEMORY_POOLS_G1_EDEN = JVM_MEMORY + ".pools.G1-Eden-Space.usage";
	public static final String JVM_MEMORY_POOLS_G1_OLDGEN = JVM_MEMORY + ".pools.G1-Old-Gen.usage";
	public static final String JVM_MEMORY_POOLS_G1_PERMGEN = JVM_MEMORY + ".pools.G1-Perm-Gen.usage";
	public static final String JVM_MEMORY_POOLS_G1_SURVIVOR = JVM_MEMORY + ".pools.G1-Survivor-Space.usage";

	@SuppressWarnings("rawtypes")
	public static long getLong(MetricRegistry registry, String key) {
		SortedMap<String, Gauge> gauges = registry.getGauges();
		Gauge gauge = gauges.get(key);
		if (gauge == null) {
			return 0;
		}
		Object value = gauge.getValue();
		if (value == null) {
			return 0;
		} else if (value instanceof Long || value instanceof Integer) {
			return (Long) value;
		} else {
			return 0;
		}
	}
	
}
