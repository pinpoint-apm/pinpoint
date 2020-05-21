/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.provider.stat.totalthread;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.UnsupportedMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.totalthread.DefaultTotalThreadMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.totalthread.TotalThreadMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.totalthread.TotalThreadMetricSnapshot;

public class TotalThreadMetricCollectorProvider implements Provider<AgentStatMetricCollector<TotalThreadMetricSnapshot>> {
    private final TotalThreadMetric totalThreadMetric;

    @Inject
    public TotalThreadMetricCollectorProvider(TotalThreadMetric totalThreadMetric) {
        this.totalThreadMetric = Assert.requireNonNull(totalThreadMetric, "totalThreadMetric");
    }

    @Override
    public AgentStatMetricCollector<TotalThreadMetricSnapshot> get() {
        if(totalThreadMetric == TotalThreadMetric.UNSUPPORTED_TOTAL_THREAD_METRIC) {
            return new UnsupportedMetricCollector<TotalThreadMetricSnapshot>();
        }
        return new DefaultTotalThreadMetricCollector(totalThreadMetric);
    }
}
