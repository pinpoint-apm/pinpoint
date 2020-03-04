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

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.google.common.collect.ImmutableMap;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Taejin Koo
 */
public class DataSourceChart implements StatChart {

    private final DataSourceChartGroup dataSourceChartGroup;

    public DataSourceChart(TimeWindow timeWindow, List<SampledDataSource> sampledDataSources, ServiceTypeRegistryService serviceTypeRegistryService) {
        this.dataSourceChartGroup = newDataSourceChartGroup(timeWindow, sampledDataSources, serviceTypeRegistryService);
    }

    @VisibleForTesting
    static DataSourceChartGroup newDataSourceChartGroup(TimeWindow timeWindow, List<SampledDataSource> sampledDataSources, ServiceTypeRegistryService serviceTypeRegistryService) {
        Objects.requireNonNull(timeWindow, "timeWindow");

        Map<StatChartGroup.ChartType, Chart<? extends Point>> chartTypeChartMap = newDatasourceChart(timeWindow, sampledDataSources);
        if (CollectionUtils.isNotEmpty(sampledDataSources)) {
            SampledDataSource latestSampledDataSource = ListUtils.getLast(sampledDataSources);

            int id = latestSampledDataSource.getId();
            String serviceTypeName = serviceTypeRegistryService.findServiceType(latestSampledDataSource.getServiceTypeCode()).getName();
            String databaseName = latestSampledDataSource.getDatabaseName();
            String jdbcUrl = latestSampledDataSource.getJdbcUrl();
            return new DataSourceChartGroup(timeWindow, chartTypeChartMap, id, serviceTypeName, databaseName, jdbcUrl);
        } else {
            final Integer uncollectedValue = SampledDataSource.UNCOLLECTED_VALUE;
            // TODO avoid null
            final String uncollectedString = SampledDataSource.UNCOLLECTED_STRING;

            return new DataSourceChartGroup(timeWindow, chartTypeChartMap, uncollectedValue, uncollectedString, uncollectedString, uncollectedString);
        }
    }

    @Override
    public StatChartGroup getCharts() {
        return dataSourceChartGroup;
    }

    public int getId() {
        return dataSourceChartGroup.getId();
    }

    public String getServiceType() {
        return dataSourceChartGroup.getServiceTypeName();
    }

    public String getDatabaseName() {
        return dataSourceChartGroup.getDatabaseName();
    }

    public String getJdbcUrl() {
        return dataSourceChartGroup.getJdbcUrl();
    }

    @VisibleForTesting
    static Map<StatChartGroup.ChartType, Chart<? extends Point>> newDatasourceChart(TimeWindow timeWindow, List<SampledDataSource> sampledDataSourceList) {
        Chart<AgentStatPoint<Integer>> activeConnectionChart = newChart(timeWindow, sampledDataSourceList, SampledDataSource::getActiveConnectionSize);
        Chart<AgentStatPoint<Integer>> maxConnectionChart = newChart(timeWindow, sampledDataSourceList, SampledDataSource::getMaxConnectionSize);

        return ImmutableMap.of(DataSourceChartGroup.DataSourceChartType.ACTIVE_CONNECTION_SIZE, activeConnectionChart, DataSourceChartGroup.DataSourceChartType.MAX_CONNECTION_SIZE, maxConnectionChart);
    }

    @VisibleForTesting
    static Chart<AgentStatPoint<Integer>> newChart(TimeWindow timeWindow, List<SampledDataSource> sampledDataSourceList, Function<SampledDataSource, AgentStatPoint<Integer>> filter) {
        TimeSeriesChartBuilder<AgentStatPoint<Integer>> builder = new TimeSeriesChartBuilder<>(timeWindow, SampledDataSource.UNCOLLECTED_POINT_CREATOR);
        return builder.build(sampledDataSourceList, filter);
    }

    public static class DataSourceChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private final Map<ChartType, Chart<? extends Point>> dataSourceCharts;

        private final int id;
        private final String serviceTypeName;
        private final String databaseName;
        private final String jdbcUrl;

        public enum DataSourceChartType implements AgentChartType {
            ACTIVE_CONNECTION_SIZE,
            MAX_CONNECTION_SIZE
        }

        public DataSourceChartGroup(TimeWindow timeWindow, Map<ChartType, Chart<? extends Point>> dataSourceCharts, int id, String serviceTypeName, String databaseName, String jdbcUrl) {
            this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
            this.dataSourceCharts = dataSourceCharts;
            this.id = id;
            this.serviceTypeName = serviceTypeName;
            this.databaseName = databaseName;
            this.jdbcUrl = jdbcUrl;
        }



        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
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

}
