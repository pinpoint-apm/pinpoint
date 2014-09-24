package com.nhn.pinpoint.profiler.monitor.codahale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import com.nhn.pinpoint.profiler.monitor.CounterMonitor;
import com.nhn.pinpoint.profiler.monitor.EventRateMonitor;
import com.nhn.pinpoint.profiler.monitor.HistogramMonitor;
import com.nhn.pinpoint.profiler.monitor.MonitorName;
import com.nhn.pinpoint.thrift.dto.TAgentStat;
import org.apache.thrift.meta_data.FieldMetaData;
import org.junit.Test;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.nhn.pinpoint.thrift.dto.TAgentStat._Fields;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricHistogramMonitor;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricMonitorRegistryTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


	MetricMonitorRegistry registry = new MetricMonitorRegistry();

	@Test
	public void counter() {
		CounterMonitor counter = registry.newCounterMonitor(new MonitorName("test.counter"));

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
				.newEventRateMonitor(new MonitorName("test.eventrate"));

		assertEquals(0, eventRate.getCount());
		eventRate.event();
		assertEquals(1, eventRate.getCount());
		eventRate.events(100);
		assertEquals(101, eventRate.getCount());
	}

	@Test
	public void histogram() {
		HistogramMonitor histogram = registry
				.newHistogramMonitor(new MonitorName("test.histogram"));

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
		registry.registerJvmMemoryMonitor(new MonitorName("jvm.memory"));
		registry.registerJvmGcMonitor(new MonitorName("jvm.gc"));
		registry.registerJvmAttributeMonitor(new MonitorName("jvm.vm"));
		registry.registerJvmThreadStatesMonitor(new MonitorName("jvm.thread"));

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

	String toMetricName(String name) {
		return name.toLowerCase().replace("non_", "non-").replace("_", ".");
	}
	
	@Test
	public void mapper() {
		TAgentStat agentStat = new TAgentStat();
		
		MetricRegistry r = registry.getRegistry();
		Map<String, Gauge> map = r.getGauges();
//		for (Entry<String, Gauge> each : map.entrySet()) {
//			logger.debug(each.getKey() + " : " + each.getValue().getValue().getClass());
//		}
//		
		for (Entry<_Fields, FieldMetaData> each : TAgentStat.metaDataMap.entrySet()) {
			logger.debug(toMetricName(each.getKey().name()));
			Gauge value = map.get(toMetricName(each.getKey().name()));
			if (value != null) {
				agentStat.setFieldValue(each.getKey(), value.getValue());
			}
		}

        logger.debug("{}", agentStat);
	}
	
}
