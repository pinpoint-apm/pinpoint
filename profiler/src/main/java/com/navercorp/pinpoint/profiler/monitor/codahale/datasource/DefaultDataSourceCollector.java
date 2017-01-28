/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.codahale.datasource;

import com.codahale.metrics.Metric;
import com.navercorp.pinpoint.profiler.monitor.codahale.datasource.metric.DataSourceGauge;
import com.navercorp.pinpoint.profiler.monitor.codahale.datasource.metric.DataSourceMetricSet;
import com.navercorp.pinpoint.thrift.dto.TDataSource;
import com.navercorp.pinpoint.thrift.dto.TDataSourceList;

import java.util.Map;

/**
 * @author Taejin Koo
 */
public class DefaultDataSourceCollector implements DataSourceCollector {

    private final DataSourceMetricSet dataSourceMetricSet;

    public DefaultDataSourceCollector(DataSourceMetricSet dataSourceMetricSet) {
        this.dataSourceMetricSet = dataSourceMetricSet;
    }

    @Override
    public TDataSourceList collect() {
        TDataSourceList dataSourceList = new TDataSourceList();

        Map<String, Metric> metrics = dataSourceMetricSet.getMetrics();
        for (Metric metric : metrics.values()) {
            if (metric instanceof DataSourceGauge) {
                TDataSource dataSource = ((DataSourceGauge) metric).getValue();
                dataSourceList.addToDataSourceList(dataSource);
            }
        }

        return dataSourceList;
    }

}
