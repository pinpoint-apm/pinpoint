package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import static org.junit.Assert.*;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import com.nhn.pinpoint.profiler.monitor.MonitorName;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GarbageCollectorTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private GarbageCollector collector = new GarbageCollector();
	private MetricMonitorRegistry registry = new MetricMonitorRegistry();
	
	@Test
	public void test() {
		registry.registerJvmGcMonitor(new MonitorName("jvm.gc"));
		registry.registerJvmMemoryMonitor(new MonitorName("jvm.memory"));
		
        collector.setType(registry);
        logger.debug("collector.getType():{}", collector.getType());
    }
}
