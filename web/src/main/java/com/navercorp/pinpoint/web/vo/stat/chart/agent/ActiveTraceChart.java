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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static class ActiveTraceChartGroup implements StatChartGroup{

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
            this.activeTraceCharts = new HashMap<>();
            List<AgentStatPoint<Integer>> fastCounts = new ArrayList<>(sampledActiveTraces.size());
            List<AgentStatPoint<Integer>> normalCounts = new ArrayList<>(sampledActiveTraces.size());
            List<AgentStatPoint<Integer>> slowCounts = new ArrayList<>(sampledActiveTraces.size());
            List<AgentStatPoint<Integer>> verySlowCounts = new ArrayList<>(sampledActiveTraces.size());
            for (SampledActiveTrace sampledActiveTrace : sampledActiveTraces) {
                fastCounts.add(sampledActiveTrace.getFastCounts());
                normalCounts.add(sampledActiveTrace.getNormalCounts());
                slowCounts.add(sampledActiveTrace.getSlowCounts());
                verySlowCounts.add(sampledActiveTrace.getVerySlowCounts());
            }
            TimeSeriesChartBuilder<AgentStatPoint<Integer>> chartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledActiveTrace.UNCOLLECTED_POINT_CREATER);
            activeTraceCharts.put(ActiveTraceChartType.ACTIVE_TRACE_FAST, chartBuilder.build(fastCounts));
            activeTraceCharts.put(ActiveTraceChartType.ACTIVE_TRACE_NORMAL, chartBuilder.build(normalCounts));
            activeTraceCharts.put(ActiveTraceChartType.ACTIVE_TRACE_SLOW, chartBuilder.build(slowCounts));
            activeTraceCharts.put(ActiveTraceChartType.ACTIVE_TRACE_VERY_SLOW, chartBuilder.build(verySlowCounts));
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
