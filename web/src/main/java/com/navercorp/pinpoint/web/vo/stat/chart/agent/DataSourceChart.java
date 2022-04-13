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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DataSourceChart implements StatChart<AgentStatPoint<Integer>> {

    public enum DataSourceChartType implements StatChartGroup.AgentChartType {
        ACTIVE_CONNECTION_SIZE,
        MAX_CONNECTION_SIZE
    }

    private final DataSourceChartGroup dataSourceChartGroup;

    private static final ChartGroupBuilder<SampledDataSource, AgentStatPoint<Integer>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledDataSource, AgentStatPoint<Integer>> newChartBuilder() {
        ChartGroupBuilder<SampledDataSource, AgentStatPoint<Integer>> builder = new ChartGroupBuilder<>(SampledDataSource.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(DataSourceChartType.ACTIVE_CONNECTION_SIZE, SampledDataSource::getActiveConnectionSize);
        builder.addPointFunction(DataSourceChartType.MAX_CONNECTION_SIZE, SampledDataSource::getMaxConnectionSize);
        return builder;
    }

    public DataSourceChart(TimeWindow timeWindow, List<SampledDataSource> sampledDataSources, ServiceTypeRegistryService serviceTypeRegistryService) {
        this.dataSourceChartGroup = newDataSourceChartGroup(timeWindow, sampledDataSources, serviceTypeRegistryService);
    }

    @VisibleForTesting
    static DataSourceChartGroup newDataSourceChartGroup(TimeWindow timeWindow, List<SampledDataSource> sampledDataSources, ServiceTypeRegistryService serviceTypeRegistryService) {
        Objects.requireNonNull(timeWindow, "timeWindow");

        Map<StatChartGroup.ChartType, Chart<AgentStatPoint<Integer>>> chartTypeChartMap = newDatasourceChart(timeWindow, sampledDataSources);
        if (CollectionUtils.isEmpty(sampledDataSources)) {
            final Integer uncollectedValue = SampledDataSource.UNCOLLECTED_VALUE;
            // TODO avoid null
            final String uncollectedString = SampledDataSource.UNCOLLECTED_STRING;

            return new DataSourceChartGroup(timeWindow, chartTypeChartMap, uncollectedValue, uncollectedString, uncollectedString, uncollectedString);
        } else {
            SampledDataSource latestSampledDataSource = CollectionUtils.lastElement(sampledDataSources);

            int id = latestSampledDataSource.getId();
            String serviceTypeName = serviceTypeRegistryService.findServiceType(latestSampledDataSource.getServiceTypeCode()).getName();
            String databaseName = latestSampledDataSource.getDatabaseName();
            String jdbcUrl = latestSampledDataSource.getJdbcUrl();
            return new DataSourceChartGroup(timeWindow, chartTypeChartMap, id, serviceTypeName, databaseName, jdbcUrl);
        }
    }

    @Override
    public StatChartGroup<AgentStatPoint<Integer>> getCharts() {
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
    static Map<StatChartGroup.ChartType, Chart<AgentStatPoint<Integer>>> newDatasourceChart(TimeWindow timeWindow, List<SampledDataSource> sampledDataSourceList) {
        return BUILDER.buildMap(timeWindow, sampledDataSourceList);
    }

    public static class DataSourceChartGroup implements StatChartGroup<AgentStatPoint<Integer>> {

        private final TimeWindow timeWindow;

        private final Map<ChartType, Chart<AgentStatPoint<Integer>>> dataSourceCharts;

        private final int id;
        private final String serviceTypeName;
        private final String databaseName;
        private final String jdbcUrl;

        public DataSourceChartGroup(TimeWindow timeWindow, Map<ChartType, Chart<AgentStatPoint<Integer>>> dataSourceCharts, int id, String serviceTypeName, String databaseName, String jdbcUrl) {
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
        public Map<ChartType, Chart<AgentStatPoint<Integer>>> getCharts() {
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
