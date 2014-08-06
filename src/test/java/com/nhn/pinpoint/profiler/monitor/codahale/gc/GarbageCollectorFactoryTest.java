package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.nhn.pinpoint.thrift.dto.TJvmGc;

import org.junit.Test;

import com.nhn.pinpoint.profiler.monitor.codahale.AgentStatCollectorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GarbageCollectorFactoryTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void test() {
        GarbageCollector collector = new AgentStatCollectorFactory().getGarbageCollector();

        logger.debug("collector.getType():{}", collector);
        TJvmGc collect1 = collector.collect();
        logger.debug("collector.collect():{}", collect1);
        TJvmGc collect2 = collector.collect();
        logger.debug("collector.collect():{}", collect2);
    }
}
