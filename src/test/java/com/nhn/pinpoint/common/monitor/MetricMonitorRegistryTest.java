package com.nhn.pinpoint.common.monitor;

import org.junit.Test;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.nhn.pinpoint.common.monitor.CounterMonitor;
import com.nhn.pinpoint.common.monitor.EventRateMonitor;
import com.nhn.pinpoint.common.monitor.HistogramMonitor;
import com.nhn.pinpoint.common.monitor.MonitorName;
import com.nhn.pinpoint.common.monitor.codahale.MetricHistogramMonitor;
import com.nhn.pinpoint.common.monitor.codahale.MetricMonitorRegistry;

import static org.junit.Assert.*;

public class MetricMonitorRegistryTest {

	MetricMonitorRegistry registry = new MetricMonitorRegistry();

	@Test
	public void counter() {
		CounterMonitor counter = registry.newCounterMonitor(new MonitorName("test", "counter"));

		assertEquals(0, counter.getCount());
		counter.incr();
		assertEquals(1, counter.getCount());
		counter.incr(10);
		assertEquals(11, counter.getCount());
		counter.decr();
		assertEquals(10, counter.getCount());
		counter.decr(10);
		assertEquals(0, counter.getCount());
	}

	@Test
	public void eventRate() {
		EventRateMonitor eventRate = registry
				.newEventRateMonitor(new MonitorName("test", "eventrate"));

		assertEquals(0, eventRate.getCount());
		eventRate.event();
		assertEquals(1, eventRate.getCount());
		eventRate.events(100);
		assertEquals(101, eventRate.getCount());
	}

	@Test
	public void histogram() {
		HistogramMonitor histogram = registry
				.newHistogramMonitor(new MonitorName("test", "histogram"));

		histogram.update(1);
		histogram.update(10);
		histogram.update(100);
		assertEquals(3, histogram.getCount());

		Histogram h = ((MetricHistogramMonitor) histogram).getDelegate();
		Snapshot snapshot = h.getSnapshot();
		assertEquals(100, snapshot.getMax());
		assertEquals(1, snapshot.getMin());
		assertTrue(10.0 == snapshot.getMedian());
	}

	@Test
	public void jvm() {
		registry.registerJvmMemoryMonitor(new MonitorName("jvm", "memory"));
		registry.registerJvmGcMonitor(new MonitorName("jvm", "gc"));
		registry.registerJvmAttributeMonitor(new MonitorName("jvm", "vm"));
		registry.registerJvmThreadStatesMonitor(new MonitorName("jvm", "thread"));

		boolean hasMemory = false;
		boolean hasGc = false;
		boolean hasVm = false;
		boolean hasThread = false;
		
		for (String each : registry.getRegistry().getNames()) {
			if (each.startsWith("jvm.gc")) {
				hasGc = true;
			} else if (each.startsWith("jvm.memory")) {
				hasMemory = true;
			} else if (each.startsWith("jvm.vm")) {
				hasVm = true;
			} else if (each.startsWith("jvm.thread")) {
				hasThread = true;
			}
		}
		
		assertTrue(hasMemory);
		assertTrue(hasGc);
		assertTrue(hasVm);
		assertTrue(hasThread);
	}

	@Test
	public void jsonMapper() throws Exception {
		String jsonString = registry.getMonitorsAsJson();
		assertNotNull(jsonString);
		assertTrue(jsonString.contains("{"));
		System.out.println(jsonString);
		
		byte[] jsonBytes = registry.getMonitorsAsJsonBytes();
		assertNotNull(jsonBytes);
		assertTrue(jsonBytes.length > 0);
		System.out.println(jsonBytes.length + " bytes");
	}

}
