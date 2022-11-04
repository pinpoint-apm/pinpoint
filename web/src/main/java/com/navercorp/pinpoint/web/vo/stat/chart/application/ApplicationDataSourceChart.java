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
package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class ApplicationDataSourceChart implements StatChart<IntApplicationStatPoint> {
    private static final Point.UncollectedPointCreator<IntApplicationStatPoint> UNCOLLECTED_POINT
            = new IntApplicationStatPoint.UncollectedCreator(JoinDataSourceBo.UNCOLLECTED_VALUE);

    public enum DataSourceChartType implements StatChartGroup.ApplicationChartType {
        ACTIVE_CONNECTION_SIZE
    }

    private final ApplicationDataSourceChartGroup applicationDataSourceChartGroup;

    private static final ChartGroupBuilder<AggreJoinDataSourceBo, IntApplicationStatPoint> BUILDER = newChartBuilder();

    static ChartGroupBuilder<AggreJoinDataSourceBo, IntApplicationStatPoint> newChartBuilder() {
        ChartGroupBuilder<AggreJoinDataSourceBo, IntApplicationStatPoint> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(DataSourceChartType.ACTIVE_CONNECTION_SIZE, ApplicationDataSourceChartGroup::newDataSource);
        return builder;
    }

    public ApplicationDataSourceChart(TimeWindow timeWindow, String url, String serviceTypeCodeName, List<AggreJoinDataSourceBo> appDataSourceBoList) {
        this.applicationDataSourceChartGroup = new ApplicationDataSourceChartGroup(timeWindow, url, serviceTypeCodeName, appDataSourceBoList);
    }

    @Override
    public StatChartGroup<IntApplicationStatPoint> getCharts() {
        return applicationDataSourceChartGroup;
    }

    public String getServiceType() {
        return applicationDataSourceChartGroup.getServiceTypeCodeName();
    }

    public String getJdbcUrl() {
        return applicationDataSourceChartGroup.getJdbcUrl();
    }

    public static class ApplicationDataSourceChartGroup implements StatChartGroup<IntApplicationStatPoint> {

        private final TimeWindow timeWindow;
        private final String url;
        private final String serviceTypeCodeName;
        private final Map<ChartType, Chart<IntApplicationStatPoint>> dataSourceChartMap;

        public ApplicationDataSourceChartGroup(TimeWindow timeWindow, String url, String serviceTypeCodeName, List<AggreJoinDataSourceBo> appStatList) {
            this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
            this.url = url;
            this.serviceTypeCodeName = serviceTypeCodeName;
            this.dataSourceChartMap = newChart(appStatList);
        }

        private Map<ChartType, Chart<IntApplicationStatPoint>> newChart(List<AggreJoinDataSourceBo> appStatList) {
            StatChartGroup<IntApplicationStatPoint> group = BUILDER.build(timeWindow, appStatList);
            return group.getCharts();
        }


        private static IntApplicationStatPoint newDataSource(AggreJoinDataSourceBo ds) {
            final JoinIntFieldBo activeConnectionSizeJoinValue = ds.getActiveConnectionSizeJoinValue();
            long timestamp = ds.getTimestamp();
            return StatPointUtils.toIntStatPoint(timestamp, activeConnectionSizeJoinValue);
        }

        public String getJdbcUrl() {
            return url;
        }

        public String getServiceTypeCodeName() {
            return serviceTypeCodeName;
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<IntApplicationStatPoint>> getCharts() {
            return dataSourceChartMap;
        }
    }
}
