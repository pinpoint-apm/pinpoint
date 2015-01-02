/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.codahale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import com.navercorp.pinpoint.profiler.monitor.CounterMonitor;
import com.navercorp.pinpoint.profiler.monitor.EventRateMonitor;
import com.navercorp.pinpoint.profiler.monitor.HistogramMonitor;
import com.navercorp.pinpoint.profiler.monitor.MonitorName;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricHistogramMonitor;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStat._Fields;

import org.apache.thrift.meta_data.FieldMetaData;
import org.junit.Test;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricMonitorRegistryTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    MetricMonitorRegistry registry = new MetricMonitorRegistry()

	    Test
	public void cou       ter() {
		CounterMonitor counter = registry.newCounterMonitor(new MonitorName("tes       .counter"));

		assertEquals(0,        ounter.getC       unt());
		counter.incr();
		asse       tEquals(1, co       nter.getCount());
		counter.incr(       0);
		asser       Equals(11, counter.getCount());
	       counter.decr(       ;
		assertEquals(10, counter.get        unt    ));
		counter.decr(10);       		assertEquals(0, counter.getCoun             ());
	}

	@Test
	public void eventRate() {
		Even       RateMonitor eventRate = registry
	       		.newEventRat       Monitor(new MonitorName("test.even       rate"));

		assert       quals(0, eventRate.getCount());
		ev        tRa    e.event();
		assertEqua       s(1, eventRate.getCount());
		eve             tRate.events(100);
		assertEquals(101, eventRate.       etCount());
	}

       @Test
	public voi        histogram() {
		H       stogramMonitor histogram = registry       				.newHistogramMonitor(new MonitorName("test.histogram"));
       		histogram.update(1);
		histogr       m.update(10);
		histogram.update(       00);
		assertEquals(3, histogra       .getCount());

		Histogram h = ((Metr        His    ogramMonitor) his       ogram).getDelegate();
		Snapshot snapshot = h.getSnapshot();
       	assertEquals(100, snapshot.getMax());
		assertEquals       1, snapshot.getMin());
		assertTrue(10.0 == snapshot.getMedi       n());
	}

	@Test
	public void jvm() {
		registry.registerJvmMemoryMo       itor(new MonitorName("       vm.memory"));
		re       istry.registerJvmG       Monitor(new MonitorNam             ("jvm.gc"));
		registry.registerJvmAttributeMoni          or(new MonitorName("jvm.vm             ));
	          registry.registerJvmThreadStatesMonit             r(new Mon          torName("jvm.thread"));

		boolea              hasM          mory = false;
		boolean hasGc = false
		boolea                             hasVm =        alse;
		boolea        hasThread = f       lse;
		
		for (Str        g each : registry.getRegistry().       etNames()) {
			if (each.startsWith("jvm.gc")) {
				hasGc = tru          ;
    		} else if (each.st       rtsWith("jvm.memory")) {
				hasMemo             y = true;
			} else if (each.starts       ith("jvm.vm")) {
				hasVm = true;
		       } else if (each.startsWith("jvm.thread")) {
				          asThread = true;
			}
		}
		
		assertTrue(hasMemory);
		assertTrue(hasGc                   ;
		assertTrue(hasVm);
		assertTrue(hasThread);
	}

	String toMetricNam          (String name) {
		return name.toLowerCase()          replace("non_", "non-").replace("_", ".");
	}
	
	@Te          t
	public void             mapper() {
		TAgentStat agentStat = new TAgentSta                ();
		
		MetricRegistry r = regis       ry.getRegistry();
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
