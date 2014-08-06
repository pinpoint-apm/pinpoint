package com.nhn.pinpoint.profiler.monitor.codahale;

import com.nhn.pinpoint.profiler.monitor.MonitorName;
import com.nhn.pinpoint.profiler.monitor.codahale.cpu.CpuLoadCollector;
import com.nhn.pinpoint.profiler.monitor.codahale.cpu.metric.CpuLoadMetricSet;
import com.nhn.pinpoint.profiler.monitor.codahale.gc.CmsCollector;
import com.nhn.pinpoint.profiler.monitor.codahale.gc.G1Collector;
import com.nhn.pinpoint.profiler.monitor.codahale.gc.GarbageCollector;
import com.nhn.pinpoint.profiler.monitor.codahale.gc.ParallelCollector;
import com.nhn.pinpoint.profiler.monitor.codahale.gc.SerialCollector;
import com.nhn.pinpoint.profiler.monitor.codahale.gc.UnknownGarbageCollector;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

/**
 * @author emeroad
 * @author harebox
 * @author hyungil.jeong
 */
public class AgentStatCollectorFactory {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final MetricMonitorRegistry monitorRegistry;
	private final GarbageCollector garbageCollector;
	private final CpuLoadCollector cpuLoadCollector;

	/**
	 * Metrics 통계 데이터를 이용하여 가비지 컬렉터 타입을 지정한다.
	 */
	public AgentStatCollectorFactory() {
		this.monitorRegistry = createRegistry();
		this.garbageCollector = createGarbageCollector();
		this.cpuLoadCollector = createCpuLoadCollector();
	}

	private MetricMonitorRegistry createRegistry() {
		final MetricMonitorRegistry monitorRegistry = new MetricMonitorRegistry();
		return monitorRegistry;
	}

	/**
	 * 통계 키를 기반으로 생성
	 */
	private GarbageCollector createGarbageCollector() {
		MetricMonitorRegistry registry = this.monitorRegistry;
		registry.registerJvmMemoryMonitor(new MonitorName(MetricMonitorValues.JVM_MEMORY));
		registry.registerJvmGcMonitor(new MonitorName(MetricMonitorValues.JVM_GC));
		
		Collection<String> keys = registry.getRegistry().getNames();
		GarbageCollector garbageCollectorToReturn;
		if (keys.contains(JVM_GC_SERIAL_MSC_COUNT)) {
			garbageCollectorToReturn = new SerialCollector(registry);
		} else if (keys.contains(JVM_GC_PS_MS_COUNT)) {
			garbageCollectorToReturn = new ParallelCollector(registry);
		} else if (keys.contains(JVM_GC_CMS_COUNT)) {
			garbageCollectorToReturn = new CmsCollector(registry);
		} else if (keys.contains(JVM_GC_G1_OLD_COUNT)) {
			garbageCollectorToReturn = new G1Collector(registry);
		} else {
			garbageCollectorToReturn = new UnknownGarbageCollector();
		}
		if (logger.isInfoEnabled()) {
			logger.info("found : {}", garbageCollectorToReturn);
		}
		return garbageCollectorToReturn;
	}

	private CpuLoadCollector createCpuLoadCollector() {
		CpuLoadMetricSet cpuLoadMetricSet = this.monitorRegistry.registerCpuLoadMonitor(new MonitorName(MetricMonitorValues.CPU_LOAD));
		if (logger.isInfoEnabled()) {
			logger.info("loaded : {}", cpuLoadMetricSet);
		}
		return new CpuLoadCollector(cpuLoadMetricSet);
	}

	public GarbageCollector getGarbageCollector() {
		return this.garbageCollector;
	}

	public CpuLoadCollector getCpuLoadCollector() {
		return this.cpuLoadCollector;
	}

}
