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

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.google.common.collect.ImmutableMap;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledTotalThreadCount;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TotalThreadCountChart implements StatChart {
    private final TotalThreadCountChartGroup totalThreadCountChartGroup;

    public TotalThreadCountChart(TimeWindow timeWindow, List<SampledTotalThreadCount> sampledTotalThreadCounts) {
        this.totalThreadCountChartGroup = new TotalThreadCountChartGroup(timeWindow, sampledTotalThreadCounts);
    }

    @Override
    public StatChartGroup getCharts() { return totalThreadCountChartGroup; }

    public static class TotalThreadCountChartGroup implements StatChartGroup{
        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> totalThreadCountCharts;
        public enum TotalThreadCountChartType implements AgentChartType {
            TOTAL_THREAD_COUNT
        }

            public TotalThreadCountChartGroup(TimeWindow timeWindow, List<SampledTotalThreadCount> sampledTotalThreadCounts) {
            this.timeWindow = timeWindow;
            this.totalThreadCountCharts = newChart(sampledTotalThreadCounts);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledTotalThreadCount> sampledTotalThreadCounts) {
            Chart<AgentStatPoint<Long>> totalThreadCountChart = newChart(sampledTotalThreadCounts, SampledTotalThreadCount::getTotalThreadCount);
            return ImmutableMap.of(TotalThreadCountChartType.TOTAL_THREAD_COUNT, totalThreadCountChart);
        }

        private Chart<AgentStatPoint<Long>> newChart(List<SampledTotalThreadCount> sampledTotalThreadCounts, Function<SampledTotalThreadCount, AgentStatPoint<Long>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Long>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledTotalThreadCount.UNCOLLECTED_POINT_CREATOR);
            return builder.build(sampledTotalThreadCounts, function);
        }

        @Override
        public TimeWindow getTimeWindow() { return timeWindow; }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() { return totalThreadCountCharts; }
    }
}
