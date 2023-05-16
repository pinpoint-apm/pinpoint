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

package com.navercorp.pinpoint.profiler.context.provider.stat.datasource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.UnsupportedMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.datasource.DefaultDataSourceMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetricSnapshot;

import java.util.Objects;

/**
 * @author Taejin Koo
 * @author HyunGil Jeong
 */
public class DataSourceMetricCollectorProvider implements Provider<AgentStatMetricCollector<DataSourceMetricSnapshot>> {

    private final DataSourceMetric dataSourceMetric;

    @Inject
    public DataSourceMetricCollectorProvider(DataSourceMetric dataSourceMetric) {
        this.dataSourceMetric = Objects.requireNonNull(dataSourceMetric, "dataSourceMetric");
    }

    @Override
    public AgentStatMetricCollector<DataSourceMetricSnapshot> get() {
        if (dataSourceMetric == DataSourceMetric.UNSUPPORTED_DATA_SOURCE_METRIC) {
            return new UnsupportedMetricCollector<DataSourceMetricSnapshot>();
        }
        return new DefaultDataSourceMetricCollector(dataSourceMetric);
    }
}