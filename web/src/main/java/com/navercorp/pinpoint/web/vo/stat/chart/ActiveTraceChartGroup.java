/*
 * Copyright 2016 Naver Corp.
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

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class ActiveTraceChartGroup implements AgentStatChartGroup {

    private static final Integer UNCOLLECTED_VALUE = -1;

    private final Map<ChartType, Chart> activeTraceCharts;

    public enum ActiveTraceChartType implements ChartType {
        ACTIVE_TRACE_VERY_SLOW,
        ACTIVE_TRACE_SLOW,
        ACTIVE_TRACE_NORMAL,
        ACTIVE_TRACE_FAST
    }

    public ActiveTraceChartGroup(TimeWindow timeWindow, List<SampledActiveTrace> sampledActiveTraces) {
        this.activeTraceCharts = new HashMap<>();
        List<Point<Long, Integer>> fastCounts = new ArrayList<>(sampledActiveTraces.size());
        List<Point<Long, Integer>> normalCounts = new ArrayList<>(sampledActiveTraces.size());
        List<Point<Long, Integer>> slowCounts = new ArrayList<>(sampledActiveTraces.size());
        List<Point<Long, Integer>> verySlowCounts = new ArrayList<>(sampledActiveTraces.size());
        for (SampledActiveTrace sampledActiveTrace : sampledActiveTraces) {
            fastCounts.add(sampledActiveTrace.getFastCounts());
            normalCounts.add(sampledActiveTrace.getNormalCounts());
            slowCounts.add(sampledActiveTrace.getSlowCounts());
            verySlowCounts.add(sampledActiveTrace.getVerySlowCounts());
        }
        activeTraceCharts.put(ActiveTraceChartType.ACTIVE_TRACE_FAST, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(fastCounts));
        activeTraceCharts.put(ActiveTraceChartType.ACTIVE_TRACE_NORMAL, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(normalCounts));
        activeTraceCharts.put(ActiveTraceChartType.ACTIVE_TRACE_SLOW, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(slowCounts));
        activeTraceCharts.put(ActiveTraceChartType.ACTIVE_TRACE_VERY_SLOW, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(verySlowCounts));
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return this.activeTraceCharts;
    }
}
