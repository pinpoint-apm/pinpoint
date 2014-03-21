package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.nhn.pinpoint.thrift.dto.TJvmGc;
import org.junit.Test;

import com.nhn.pinpoint.profiler.monitor.MonitorName;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GarbageCollectorFactoryTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private MetricMonitorRegistry registry = new MetricMonitorRegistry();

	@Test
	public void test() {
//        registry.registerJvmGcMonitor(new MonitorName("jvm.gc"));
//        registry.registerJvmMemoryMonitor(new MonitorName("jvm.memory"));

        GarbageCollector collector = new GarbageCollectorFactory().createGarbageCollector();

        logger.debug("collector.getType():{}", collector);
        TJvmGc collect = collector.collect();
        logger.debug("collector.collect():{}", collect);
    }
}
