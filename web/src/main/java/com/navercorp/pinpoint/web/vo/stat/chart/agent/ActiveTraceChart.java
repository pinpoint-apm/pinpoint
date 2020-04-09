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

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author HyunGil Jeong
 */
public class ActiveTraceChart implements StatChart {

    private final ActiveTraceChartGroup activeTraceChartGroup;

    public ActiveTraceChart(TimeWindow timeWindow, List<SampledActiveTrace> sampledActiveTraces) {
        this.activeTraceChartGroup = new ActiveTraceChartGroup(timeWindow, sampledActiveTraces);
    }

    @Override
    public StatChartGroup getCharts() {
        return activeTraceChartGroup;
    }

    public static class ActiveTraceChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private final Map<ChartType, Chart<? extends Point>> activeTraceCharts;

        public enum ActiveTraceChartType implements AgentChartType {
            ACTIVE_TRACE_VERY_SLOW,
            ACTIVE_TRACE_SLOW,
            ACTIVE_TRACE_NORMAL,
            ACTIVE_TRACE_FAST;

            private static final String[] SCHEMA = {"min", "max", "avg", "sum", "title"};

            @Override
            public String[] getSchema() {
                return SCHEMA;
            }
        }

        public ActiveTraceChartGroup(TimeWindow timeWindow, List<SampledActiveTrace> sampledActiveTraces) {
            this.timeWindow = timeWindow;
            this.activeTraceCharts = newChart(sampledActiveTraces);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledActiveTrace> sampledActiveTraces) {

            Chart<AgentStatPoint<Integer>> fastChart = newChart(sampledActiveTraces, SampledActiveTrace::getFastCounts);
            Chart<AgentStatPoint<Integer>> normalChart = newChart(sampledActiveTraces, SampledActiveTrace::getNormalCounts);
            Chart<AgentStatPoint<Integer>> slowChart = newChart(sampledActiveTraces, SampledActiveTrace::getSlowCounts);
            Chart<AgentStatPoint<Integer>> verySlowChart = newChart(sampledActiveTraces, SampledActiveTrace::getVerySlowCounts);


            return ImmutableMap.of(ActiveTraceChartType.ACTIVE_TRACE_FAST, fastChart,
                    ActiveTraceChartType.ACTIVE_TRACE_NORMAL, normalChart,
                    ActiveTraceChartType.ACTIVE_TRACE_SLOW, slowChart,
                    ActiveTraceChartType.ACTIVE_TRACE_VERY_SLOW, verySlowChart);
        }

        private Chart<AgentStatPoint<Integer>> newChart(List<SampledActiveTrace> activeTraceList, Function<SampledActiveTrace, AgentStatPoint<Integer>> filter) {

            TimeSeriesChartBuilder<AgentStatPoint<Integer>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledActiveTrace.UNCOLLECTED_POINT_CREATOR);
            return builder.build(activeTraceList, filter);
        }


        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return activeTraceCharts;
        }
    }
}
