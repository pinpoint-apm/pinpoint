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

package com.navercorp.pinpoint.profiler.monitor.metric.datasource;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorWrapper;
import com.navercorp.pinpoint.profiler.context.monitor.JdbcUrlParsingService;
import com.navercorp.pinpoint.thrift.dto.TDataSource;
import com.navercorp.pinpoint.thrift.dto.TDataSourceList;

import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 * @author HyunGil Jeong
 */
public class DefaultDataSourceMetric implements DataSourceMetric {

    private final DataSourceMonitorRegistryService dataSourceMonitorRegistryService;
    private final JdbcUrlParsingService jdbcUrlParsingService;

    public DefaultDataSourceMetric(DataSourceMonitorRegistryService dataSourceMonitorRegistryService, JdbcUrlParsingService jdbcUrlParsingService) {
        if (dataSourceMonitorRegistryService == null) {
            throw new NullPointerException("dataSourceMonitorRegistryService must not be null");
        }
        if (jdbcUrlParsingService == null) {
            throw new NullPointerException("jdbcUrlParsingService must not be null");
        }
        this.dataSourceMonitorRegistryService = dataSourceMonitorRegistryService;
        this.jdbcUrlParsingService = jdbcUrlParsingService;

    }

    @Override
    public TDataSourceList dataSourceList() {
        TDataSourceList dataSourceList = new TDataSourceList();

        List<DataSourceMonitorWrapper> dataSourceMonitorList = dataSourceMonitorRegistryService.getPluginMonitorWrapperList();
        if (!CollectionUtils.isEmpty(dataSourceMonitorList)) {
            for (DataSourceMonitorWrapper dataSourceMonitor : dataSourceMonitorList) {
                TDataSource dataSource = collectDataSource(dataSourceMonitor);
                dataSourceList.addToDataSourceList(dataSource);
            }
        } else {
            dataSourceList.setDataSourceList(Collections.<TDataSource>emptyList());
        }
        return dataSourceList;
    }

    @Override
    public String toString() {
        return "Default DataSourceMetric";
    }

    private TDataSource collectDataSource(DataSourceMonitorWrapper dataSourceMonitor) {
        TDataSource dataSource = new TDataSource();
        dataSource.setId(dataSourceMonitor.getId());
        dataSource.setServiceTypeCode(dataSourceMonitor.getServiceType().getCode());

        String jdbcUrl = dataSourceMonitor.getUrl();
        if (jdbcUrl != null) {
            dataSource.setUrl(jdbcUrl);

            DatabaseInfo databaseInfo = jdbcUrlParsingService.getDatabaseInfo(jdbcUrl);
            if (databaseInfo != null) {
                dataSource.setDatabaseName(databaseInfo.getDatabaseId());
            }
        }

        int activeConnectionSize = dataSourceMonitor.getActiveConnectionSize();
        // this field is optional (default value is 0)
        if (activeConnectionSize != 0) {
            dataSource.setActiveConnectionSize(activeConnectionSize);
        }

        dataSource.setMaxConnectionSize(dataSourceMonitor.getMaxConnectionSize());

        return dataSource;
    }
}
