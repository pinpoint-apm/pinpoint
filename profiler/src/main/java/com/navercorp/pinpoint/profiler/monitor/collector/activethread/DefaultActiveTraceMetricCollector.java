/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.collector.activethread;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.activethread.ActiveTraceMetric;

/**
 * @author HyunGil Jeong
 */
public class DefaultActiveTraceMetricCollector implements AgentStatMetricCollector<ActiveTraceHistogram> {

    private final ActiveTraceMetric activeTraceMetric;

    public DefaultActiveTraceMetricCollector(ActiveTraceMetric activeTraceMetric) {
        this.activeTraceMetric = Assert.requireNonNull(activeTraceMetric, "activeTraceMetric");
    }

    @Override
    public ActiveTraceHistogram collect() {
        final ActiveTraceHistogram histogram = activeTraceMetric.activeTraceHistogram();
        return histogram;
    }
}
