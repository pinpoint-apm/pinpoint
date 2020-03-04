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

package com.navercorp.pinpoint.profiler.monitor.collector.datasource;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSource;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetricSnapshot;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class DefaultDataSourceMetricCollector implements AgentStatMetricCollector<DataSourceMetricSnapshot> {

    private final DataSourceMetric dataSourceMetric;

    public DefaultDataSourceMetricCollector(DataSourceMetric dataSourceMetric) {
        this.dataSourceMetric = Assert.requireNonNull(dataSourceMetric, "dataSourceMetric");
    }

    @Override
    public DataSourceMetricSnapshot collect() {
        final List<DataSource> dataSources = dataSourceMetric.dataSourceList();

        if (CollectionUtils.isEmpty(dataSources)) {
            return new DataSourceMetricSnapshot();
        }

        final DataSourceMetricSnapshot dataSourceMetricSnapshot = new DataSourceMetricSnapshot();
        for (DataSource dataSource : dataSources) {
            dataSourceMetricSnapshot.addDataSourceCollectData(dataSource);
        }

        return dataSourceMetricSnapshot;
    }
}
