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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTotalThreadCountBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ApplicationTotalThreadCountChart implements StatChart {
    private final ApplicationTotalThreadCountChartGroup applicationTotalThreadCountChartGroup;

    public ApplicationTotalThreadCountChart(TimeWindow timeWindow, List<AggreJoinTotalThreadCountBo> aggreJoinTotalThraedCountBoList) {
        this.applicationTotalThreadCountChartGroup = new ApplicationTotalThreadCountChartGroup(timeWindow, aggreJoinTotalThraedCountBoList);
    }

    @Override
    public StatChartGroup getCharts() {
        return applicationTotalThreadCountChartGroup;
    }

    public static class ApplicationTotalThreadCountChartGroup implements StatChartGroup {

        private static final LongApplicationStatPoint.UncollectedCreator UNCOLLECTED_TOTAL_THREAD_COUNT_POINT = new LongApplicationStatPoint.UncollectedCreator(JoinTotalThreadCountBo.UNCOLLECTED_VALUE);

        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> totalThreadCountChartMap;


        public enum TotalThreadCountChartType implements ApplicationChartType {
            TOTAL_THREAD_COUNT
        }

        public ApplicationTotalThreadCountChartGroup(TimeWindow timeWindow, List<AggreJoinTotalThreadCountBo> aggreJoinTotalThraedCountBoList) {
            this.timeWindow = timeWindow;
            this.totalThreadCountChartMap = newChart(aggreJoinTotalThraedCountBoList);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<AggreJoinTotalThreadCountBo> aggreJoinTotalThreadCountBoList) {
            Chart<LongApplicationStatPoint> totalThreadCountPointChart = newChart(aggreJoinTotalThreadCountBoList, this::newTotalThreadCount);
            return ImmutableMap.of(TotalThreadCountChartType.TOTAL_THREAD_COUNT, totalThreadCountPointChart);
        }

        private Chart<LongApplicationStatPoint> newChart(List<AggreJoinTotalThreadCountBo> aggreJoinTotalThraedCountBoList, Function<AggreJoinTotalThreadCountBo, LongApplicationStatPoint> filter) {
            TimeSeriesChartBuilder<LongApplicationStatPoint> builder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_TOTAL_THREAD_COUNT_POINT);
            return builder.build(aggreJoinTotalThraedCountBoList, filter);
        }

        private LongApplicationStatPoint newTotalThreadCount(AggreJoinTotalThreadCountBo totalThreadCountBo) {
            final JoinLongFieldBo totalThreadCountJoinValue = totalThreadCountBo.getTotalThreadCountJoinValue();
            return new LongApplicationStatPoint(totalThreadCountBo.getTimestamp(), totalThreadCountJoinValue.getMin(), totalThreadCountJoinValue.getMinAgentId(),
                    totalThreadCountJoinValue.getMax(), totalThreadCountJoinValue.getMaxAgentId(), totalThreadCountJoinValue.getAvg());
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() { return totalThreadCountChartMap; }
    }
}
