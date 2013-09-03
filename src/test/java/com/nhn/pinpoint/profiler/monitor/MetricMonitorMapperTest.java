package com.nhn.pinpoint.profiler.monitor;

import static org.junit.Assert.*;

import org.junit.Test;

import com.nhn.pinpoint.common.dto.thrift.AgentStat;
import com.nhn.pinpoint.common.dto.thrift.StatWithCmsCollector;
import com.nhn.pinpoint.profiler.monitor.MonitorName;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorMapper;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;

/**
 * @author harebox
 */
public class MetricMonitorMapperTest {

	MetricMonitorMapper mapper = new MetricMonitorMapper();
	
	@Test
	public void convertName() {
		assertEquals("jvm.memory.total.init", mapper.convertName("JVM_MEMORY_TOTAL_INIT"));
		assertEquals("jvm.memory.non-heap.init", mapper.convertName("JVM_MEMORY_NON_HEAP_INIT"));
	}
	
	@Test
	public void map() {
		MetricMonitorRegistry registry = new MetricMonitorRegistry();
		registry.registerJvmMemoryMonitor(new MonitorName("jvm", "memory"));
		registry.registerJvmGcMonitor(new MonitorName("jvm", "gc"));
		
		// when
		AgentStat agentStat = new AgentStat();
		assertNull(agentStat.getSetField());

		StatWithCmsCollector cms = new StatWithCmsCollector();
		agentStat.setCms(cms);
		
		// test
		mapper.map(registry, agentStat);
		
		// then
		assertTrue(0 < agentStat.getCms().getJvmMemoryTotalMax());
		
		System.out.println(agentStat);
	}
	
}
