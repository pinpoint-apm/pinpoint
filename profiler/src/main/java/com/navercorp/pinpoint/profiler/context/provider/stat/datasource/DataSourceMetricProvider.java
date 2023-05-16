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
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.JdbcUrlParsingService;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DefaultDataSourceMetric;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author Taejin Koo
 * @author HyunGil Jeong
 */
public class DataSourceMetricProvider implements Provider<DataSourceMetric> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final DataSourceMonitorRegistryService dataSourceMonitorRegistryService;
    private final JdbcUrlParsingService jdbcUrlParsingService;

    @Inject
    public DataSourceMetricProvider(DataSourceMonitorRegistryService dataSourceMonitorRegistryService, JdbcUrlParsingService jdbcUrlParsingService) {
        this.dataSourceMonitorRegistryService = dataSourceMonitorRegistryService;
        this.jdbcUrlParsingService = jdbcUrlParsingService;
    }

    @Override
    public DataSourceMetric get() {
        final DataSourceMetric dataSourceMetric = newDataSourceMetric();
        logger.info("loaded : {}", dataSourceMetric);
        return dataSourceMetric;
    }

    private DataSourceMetric newDataSourceMetric() {
        if (dataSourceMonitorRegistryService == null || jdbcUrlParsingService == null) {
            return DataSourceMetric.UNSUPPORTED_DATA_SOURCE_METRIC;
        }

        return new DefaultDataSourceMetric(dataSourceMonitorRegistryService, jdbcUrlParsingService);
    }
}
