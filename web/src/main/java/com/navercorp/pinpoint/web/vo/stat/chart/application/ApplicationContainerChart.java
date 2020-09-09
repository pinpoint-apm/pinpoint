/*
 * Copyright 2020 NAVER Corp.
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

import com.google.common.collect.ImmutableMap;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinContainerBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinContainerBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Hyunjoon Cho
 */
public class ApplicationContainerChart implements StatChart {

    private final ApplicationContainerChartGroup containerChartGroup;

    public ApplicationContainerChart(TimeWindow timeWindow, List<AggreJoinContainerBo> aggreJoinContainerBoList) {
        this.containerChartGroup = new ApplicationContainerChartGroup(timeWindow, aggreJoinContainerBoList);
    }

    @Override
    public StatChartGroup getCharts() {
        return containerChartGroup;
    }

    public static class ApplicationContainerChartGroup implements StatChartGroup {

        private static final DoubleApplicationStatPoint.UncollectedCreator UNCOLLECTED_CONTAINER_DOUBLE_POINT = new DoubleApplicationStatPoint.UncollectedCreator(JoinContainerBo.UNCOLLECTED_PERCENT_USAGE);
        private static final LongApplicationStatPoint.UncollectedCreator UNCOLLECTED_CONTAINER_LONG_POINT = new LongApplicationStatPoint.UncollectedCreator(JoinContainerBo.UNCOLLECTED_MEMORY);

        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> containerChartMap;

        public enum ContainerChartType implements ApplicationChartType {
            USER_CPU_USAGE,
            SYSTEM_CPU_USAGE,
            MEMORY_MAX,
            MEMORY_USAGE
        }

        public ApplicationContainerChartGroup(TimeWindow timeWindow, List<AggreJoinContainerBo> aggreJoinContainerBoList) {
            this.timeWindow = timeWindow;
            this.containerChartMap = newChart(aggreJoinContainerBoList);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<AggreJoinContainerBo> aggreContainerBoList) {
            Chart<DoubleApplicationStatPoint> userCpuUsageChart = newDoubleChart(aggreContainerBoList, this::newUserCpuUsage);
            Chart<DoubleApplicationStatPoint> systemCpuUsageChart = newDoubleChart(aggreContainerBoList, this::newSystemCpuUsage);
            Chart<LongApplicationStatPoint> memoryMaxChart = newLongChart(aggreContainerBoList, this::newMemoryMax);
            Chart<LongApplicationStatPoint> memoryUsageChart = newLongChart(aggreContainerBoList, this::newMemoryUsage);
            return ImmutableMap.of(ContainerChartType.USER_CPU_USAGE, userCpuUsageChart
                    , ContainerChartType.SYSTEM_CPU_USAGE, systemCpuUsageChart
                    , ContainerChartType.MEMORY_MAX, memoryMaxChart
                    , ContainerChartType.MEMORY_USAGE, memoryUsageChart);
        }

        private Chart<DoubleApplicationStatPoint> newDoubleChart(List<AggreJoinContainerBo> containerList, Function<AggreJoinContainerBo, DoubleApplicationStatPoint> filter) {

            TimeSeriesChartBuilder<DoubleApplicationStatPoint> builder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_CONTAINER_DOUBLE_POINT);
            return builder.build(containerList, filter);
        }

        private Chart<LongApplicationStatPoint> newLongChart(List<AggreJoinContainerBo> containerList, Function<AggreJoinContainerBo, LongApplicationStatPoint> filter) {

            TimeSeriesChartBuilder<LongApplicationStatPoint> builder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_CONTAINER_LONG_POINT);
            return builder.build(containerList, filter);
        }

        private DoubleApplicationStatPoint newUserCpuUsage(AggreJoinContainerBo container) {
            final JoinDoubleFieldBo userCpuUsageJoinValue = container.getUserCpuUsageJoinValue();
            return new DoubleApplicationStatPoint(container.getTimestamp(), userCpuUsageJoinValue.getMin(), userCpuUsageJoinValue.getMinAgentId(), userCpuUsageJoinValue.getMax(), userCpuUsageJoinValue.getMaxAgentId(), userCpuUsageJoinValue.getAvg());
        }

        private DoubleApplicationStatPoint newSystemCpuUsage(AggreJoinContainerBo container) {
            final JoinDoubleFieldBo systemCpuUsageJoinValue = container.getSystemCpuUsageJoinValue();
            return new DoubleApplicationStatPoint(container.getTimestamp(), systemCpuUsageJoinValue.getMin(), systemCpuUsageJoinValue.getMinAgentId(), systemCpuUsageJoinValue.getMax(), systemCpuUsageJoinValue.getMaxAgentId(), systemCpuUsageJoinValue.getAvg());
        }

        private LongApplicationStatPoint newMemoryMax(AggreJoinContainerBo container) {
            final JoinLongFieldBo memoryMaxJoinValue = container.getMemoryMaxJoinValue();
            return new LongApplicationStatPoint(container.getTimestamp(), memoryMaxJoinValue.getMin(), memoryMaxJoinValue.getMinAgentId(), memoryMaxJoinValue.getMax(), memoryMaxJoinValue.getMaxAgentId(), memoryMaxJoinValue.getAvg());
        }

        private LongApplicationStatPoint newMemoryUsage(AggreJoinContainerBo container) {
            final JoinLongFieldBo memoryUsageJoinValue = container.getMemoryUsageJoinValue();
            return new LongApplicationStatPoint(container.getTimestamp(), memoryUsageJoinValue.getMin(), memoryUsageJoinValue.getMinAgentId(), memoryUsageJoinValue.getMax(), memoryUsageJoinValue.getMaxAgentId(), memoryUsageJoinValue.getAvg());
        }

        @Override
        public TimeWindow getTimeWindow() {
            return this.timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return this.containerChartMap;
        }
    }
}