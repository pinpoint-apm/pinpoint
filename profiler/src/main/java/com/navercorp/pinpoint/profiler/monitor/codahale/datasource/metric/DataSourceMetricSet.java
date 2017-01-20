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

package com.navercorp.pinpoint.profiler.monitor.codahale.datasource.metric;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorWrapper;
import com.navercorp.pinpoint.profiler.context.monitor.PluginMonitorWrapperLocator;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class DataSourceMetricSet implements MetricSet {

    private final PluginMonitorWrapperLocator<DataSourceMonitorWrapper> dataSourceMonitorLocator;

    public DataSourceMetricSet(PluginMonitorWrapperLocator<DataSourceMonitorWrapper> dataSourceMonitorLocator) {
        this.dataSourceMonitorLocator = dataSourceMonitorLocator;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        List<DataSourceMonitorWrapper> dataSourceMonitorList = dataSourceMonitorLocator.getPluginMonitorWrapperList();

        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        for (DataSourceMonitorWrapper dataSourceMonitor : dataSourceMonitorList) {
            gauges.put(MetricMonitorValues.DATASOURCE + "." + dataSourceMonitor.getId(), new DataSourceGauge(dataSourceMonitor));
        }

        return gauges;
    }

}
