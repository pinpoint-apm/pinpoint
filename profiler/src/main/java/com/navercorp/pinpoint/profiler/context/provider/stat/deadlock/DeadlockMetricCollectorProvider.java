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

package com.navercorp.pinpoint.profiler.context.provider.stat.deadlock;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.UnsupportedMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.deadlock.DefaultDeadlockMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetricSnapshot;

/**
 * @author Taejin Koo
 */
public class DeadlockMetricCollectorProvider implements Provider<AgentStatMetricCollector<DeadlockMetricSnapshot>> {

    private final DeadlockMetric deadlockMetric;

    @Inject
    public DeadlockMetricCollectorProvider(DeadlockMetric deadlockMetric) {
        this.deadlockMetric = Assert.requireNonNull(deadlockMetric, "deadlockMetric");
    }

    @Override
    public AgentStatMetricCollector<DeadlockMetricSnapshot> get() {
        if (deadlockMetric == DeadlockMetric.UNSUPPORTED_DEADLOCK_SOURCE_METRIC) {
            return new UnsupportedMetricCollector<DeadlockMetricSnapshot>();
        }
        return new DefaultDeadlockMetricCollector(deadlockMetric);
    }

}
