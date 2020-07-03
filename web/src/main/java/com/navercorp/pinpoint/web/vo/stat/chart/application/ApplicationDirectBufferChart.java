/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDirectBufferBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Roy Kim
 */
public class ApplicationDirectBufferChart implements StatChart {

    private final ApplicationDirectBufferChartGroup directBufferChartGroup;

    public ApplicationDirectBufferChart(TimeWindow timeWindow, List<AggreJoinDirectBufferBo> aggreJoinDirectBufferBoList) {
        this.directBufferChartGroup = new ApplicationDirectBufferChartGroup(timeWindow, aggreJoinDirectBufferBoList);
    }

    @Override
    public StatChartGroup getCharts() {
        return directBufferChartGroup;
    }

    public static class ApplicationDirectBufferChartGroup implements StatChartGroup {

        private static final LongApplicationStatPoint.UncollectedCreator UNCOLLECTED_FILE_DESCRIPTOR_POINT = new LongApplicationStatPoint.UncollectedCreator(JoinDirectBufferBo.UNCOLLECTED_VALUE);

        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> directBufferChartMap;

        public enum DirectBufferChartType implements ApplicationChartType {
            DIRECT_COUNT,
            DIRECT_MEMORY_USED,
            MAPPED_COUNT,
            MAPPED_MEMORY_USED
        }

        public ApplicationDirectBufferChartGroup(TimeWindow timeWindow, List<AggreJoinDirectBufferBo> aggreDirectBufferList) {
            this.timeWindow = timeWindow;
            this.directBufferChartMap = newChart(aggreDirectBufferList);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<AggreJoinDirectBufferBo> aggreDirectBufferList) {
            Chart<LongApplicationStatPoint> directCountChart = newChart(aggreDirectBufferList, this::newDirectCount);
            Chart<LongApplicationStatPoint> directMemoryUsedChart = newChart(aggreDirectBufferList, this::newDirectMemoryUsed);
            Chart<LongApplicationStatPoint> mappedCountChart = newChart(aggreDirectBufferList, this::newMappedCount);
            Chart<LongApplicationStatPoint> mappedMemoryUsedChart = newChart(aggreDirectBufferList, this::newMappedMemoryUsed);
            return ImmutableMap.of(DirectBufferChartType.DIRECT_COUNT, directCountChart
                    , DirectBufferChartType.DIRECT_MEMORY_USED, directMemoryUsedChart
                    , DirectBufferChartType.MAPPED_COUNT, mappedCountChart
                    , DirectBufferChartType.MAPPED_MEMORY_USED, mappedMemoryUsedChart);
        }

        private Chart<LongApplicationStatPoint> newChart(List<AggreJoinDirectBufferBo> directBufferList, Function<AggreJoinDirectBufferBo, LongApplicationStatPoint> filter) {

            TimeSeriesChartBuilder<LongApplicationStatPoint> builder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_FILE_DESCRIPTOR_POINT);
            return builder.build(directBufferList, filter);
        }

        private LongApplicationStatPoint newDirectCount(AggreJoinDirectBufferBo directBuffer) {
            final JoinLongFieldBo directCountJoinValue = directBuffer.getDirectCountJoinValue();
            return new LongApplicationStatPoint(directBuffer.getTimestamp(), directCountJoinValue.getMin(), directCountJoinValue.getMinAgentId(), directCountJoinValue.getMax(), directCountJoinValue.getMaxAgentId(), directCountJoinValue.getAvg());
        }

        private LongApplicationStatPoint newDirectMemoryUsed(AggreJoinDirectBufferBo directBuffer) {
            final JoinLongFieldBo directMemoryUsedJoinValue = directBuffer.getDirectMemoryUsedJoinValue();
            return new LongApplicationStatPoint(directBuffer.getTimestamp(), directMemoryUsedJoinValue.getMin(), directMemoryUsedJoinValue.getMinAgentId(), directMemoryUsedJoinValue.getMax(), directMemoryUsedJoinValue.getMaxAgentId(), directMemoryUsedJoinValue.getAvg());
        }

        private LongApplicationStatPoint newMappedCount(AggreJoinDirectBufferBo directBuffer) {
            final JoinLongFieldBo mappedCountJoinValue = directBuffer.getMappedCountJoinValue();
            return new LongApplicationStatPoint(directBuffer.getTimestamp(), mappedCountJoinValue.getMin(), mappedCountJoinValue.getMinAgentId(), mappedCountJoinValue.getMax(), mappedCountJoinValue.getMaxAgentId(), mappedCountJoinValue.getAvg());
        }

        private LongApplicationStatPoint newMappedMemoryUsed(AggreJoinDirectBufferBo directBuffer) {
            final JoinLongFieldBo mappedMemoryUsedJoinValue = directBuffer.getMappedMemoryUsedJoinValue();
            return new LongApplicationStatPoint(directBuffer.getTimestamp(), mappedMemoryUsedJoinValue.getMin(), mappedMemoryUsedJoinValue.getMinAgentId(), mappedMemoryUsedJoinValue.getMax(), mappedMemoryUsedJoinValue.getMaxAgentId(), mappedMemoryUsedJoinValue.getAvg());
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return this.directBufferChartMap;
        }
    }
}
