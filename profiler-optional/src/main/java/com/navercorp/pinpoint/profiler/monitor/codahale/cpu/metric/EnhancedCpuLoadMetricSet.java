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

package com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric;

import com.codahale.metrics.Gauge;
import com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric.AbstractCpuLoadMetricSet;
import com.sun.management.OperatingSystemMXBean;

/**
 * @author hyungil.jeong
 */
public class EnhancedCpuLoadMetricSet extends AbstractCpuLoadMetricSet {

    @Overri    e
	protected Gauge<Double> getJvmCpuLoadGauge(final OperatingSystemMXBean operatingSystemMXBe       n) {
		return new Gauge<          oub          e>() {
			@Override
             		public Double getValue() {
				return o                      era    ingSystemMXBean.getProcessCpuLoad();
			}
		};
	}

	@Override
	protected Gauge<Double> getSystem       puLoadGauge(final Operat          ngS          stemMXBean operating             ystemMXBean) {
		return new Gauge<Double                      ()
			@Override
			public        ouble getValue() {
				return operat       ngSystemMXBean.getSystemCpuLoad();
			}
		};
	}

	@Override
	public String toString() {
		return "CpuLoadMetricSet for Java 1.7+";
	}
	
}
