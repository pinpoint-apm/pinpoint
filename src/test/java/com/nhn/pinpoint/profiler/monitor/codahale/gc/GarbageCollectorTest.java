package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import static org.junit.Assert.*;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import com.nhn.pinpoint.profiler.monitor.MonitorName;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;

public class GarbageCollectorTest {

	GarbageCollector collector = new GarbageCollector();
	MetricMonitorRegistry registry = new MetricMonitorRegistry(new MetricRegistry());
	
	@Test
	public void test() {
		registry.registerJvmGcMonitor(new MonitorName("jvm.gc"));
		registry.registerJvmMemoryMonitor(new MonitorName("jvm.memory"));
		
		try {
			collector.setType(registry);
			System.out.println(collector.getType());
		} catch (Exception e) {
			fail("should not be failed");
		}
	}

}
