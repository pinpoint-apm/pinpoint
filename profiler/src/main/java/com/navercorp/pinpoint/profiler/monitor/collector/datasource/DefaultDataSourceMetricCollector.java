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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSource;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetric;
import com.navercorp.pinpoint.thrift.dto.TDataSource;
import com.navercorp.pinpoint.thrift.dto.TDataSourceList;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class DefaultDataSourceMetricCollector implements AgentStatMetricCollector<TDataSourceList> {

    private final DataSourceMetric dataSourceMetric;

    public DefaultDataSourceMetricCollector(DataSourceMetric dataSourceMetric) {
        if (dataSourceMetric == null) {
            throw new NullPointerException("dataSourceMetric must not be null");
        }
        this.dataSourceMetric = dataSourceMetric;
    }

    @Override
    public TDataSourceList collect() {
        final List<DataSource> dataSources = dataSourceMetric.dataSourceList();

        if (CollectionUtils.isEmpty(dataSources)) {
            return new TDataSourceList();
        }


        TDataSourceList tDataSourceList = new TDataSourceList();
        for (DataSource dataSource : dataSources) {
            TDataSource tDataSource = toTDataSource(dataSource);
            tDataSourceList.addToDataSourceList(tDataSource);
        }

        return tDataSourceList;
    }

    private TDataSource toTDataSource(DataSource dataSource) {
        TDataSource tDataSource = new TDataSource(dataSource.getId());

        tDataSource.setServiceTypeCode(dataSource.getServiceTypeCode());

        if (dataSource.getDatabaseName() != null) {
            tDataSource.setDatabaseName(dataSource.getDatabaseName());
        }

        if (dataSource.getActiveConnectionSize() != 0) {
            tDataSource.setActiveConnectionSize(dataSource.getActiveConnectionSize());
        }

        if (dataSource.getUrl() != null) {
            tDataSource.setUrl(dataSource.getUrl());
        }

        tDataSource.setMaxConnectionSize(dataSource.getMaxConnectionSize());
        return tDataSource;
    }
}
