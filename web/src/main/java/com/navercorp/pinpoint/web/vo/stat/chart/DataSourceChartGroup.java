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

package com.navercorp.pinpoint.web.vo.stat.chart;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.view.DataSourceChartGroupSerializer;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = DataSourceChartGroupSerializer.class)
public class DataSourceChartGroup implements AgentStatChartGroup {

    private static final Integer UNCOLLECTED_VALUE = -1;
    private static final String UNCOLLECTED_STRING_VALUE = null;

    public enum DataSourceChartType implements ChartType {
        ACTIVE_CONNECTION_SIZE,
        MAX_CONNECTION_SIZE
    }

    private final Map<ChartType, Chart> dataSourceCharts;

    private final int id;
    private final String serviceTypeName;
    private final String databaseName;
    private final String jdbcUrl;

    public DataSourceChartGroup(TimeWindow timeWindow, List<SampledDataSource> sampledDataSourceList, ServiceTypeRegistryService serviceTypeRegistryService) {
        this.dataSourceCharts = new HashMap<>();

        int size = sampledDataSourceList.size();
        List<Point<Long, Integer>> activeConnectionSizes = new ArrayList<>(size);
        List<Point<Long, Integer>> maxConnectionSizes = new ArrayList<>(size);
        for (SampledDataSource sampledDataSource : sampledDataSourceList) {
            activeConnectionSizes.add(sampledDataSource.getActiveConnectionSize());
            maxConnectionSizes.add(sampledDataSource.getMaxConnectionSize());
        }
        this.dataSourceCharts.put(DataSourceChartType.ACTIVE_CONNECTION_SIZE, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(activeConnectionSizes));
        this.dataSourceCharts.put(DataSourceChartType.MAX_CONNECTION_SIZE, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(maxConnectionSizes));

        if (CollectionUtils.nullSafeSize(sampledDataSourceList) == 0) {
            this.id = UNCOLLECTED_VALUE;
            this.serviceTypeName = UNCOLLECTED_STRING_VALUE;
            this.databaseName = UNCOLLECTED_STRING_VALUE;
            this.jdbcUrl = UNCOLLECTED_STRING_VALUE;
        } else {
            SampledDataSource latestSampledDataSource = ListUtils.getLast(sampledDataSourceList);

            this.id = latestSampledDataSource.getId();
            this.serviceTypeName = serviceTypeRegistryService.findServiceType(latestSampledDataSource.getServiceTypeCode()).getName();
            this.databaseName = latestSampledDataSource.getDatabaseName();
            this.jdbcUrl = latestSampledDataSource.getJdbcUrl();
        }
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return dataSourceCharts;
    }

    public int getId() {
        return id;
    }

    public String getServiceTypeName() {
        return serviceTypeName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

}
