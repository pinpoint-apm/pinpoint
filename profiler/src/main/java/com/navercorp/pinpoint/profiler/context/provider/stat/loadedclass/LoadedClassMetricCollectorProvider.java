/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.loadedclass;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.UnsupportedMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.loadedclass.DefaultLoadedClassMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.loadedclass.LoadedClassMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.loadedclass.LoadedClassMetricSnapshot;

public class LoadedClassMetricCollectorProvider implements Provider<AgentStatMetricCollector<LoadedClassMetricSnapshot>> {
    public final LoadedClassMetric loadedClassMetric;

    @Inject
    public LoadedClassMetricCollectorProvider(LoadedClassMetric loadedClassMetric) {
        this.loadedClassMetric = Assert.requireNonNull(loadedClassMetric, "loadedClassMetric");
    }

    @Override
    public AgentStatMetricCollector<LoadedClassMetricSnapshot> get() {
        if (loadedClassMetric == LoadedClassMetric.UNSUPPORED_LOADED_CLASS_METRIC) {
            return new UnsupportedMetricCollector<LoadedClassMetricSnapshot>();
        }
        return new DefaultLoadedClassMetricCollector(loadedClassMetric);
    }
}
