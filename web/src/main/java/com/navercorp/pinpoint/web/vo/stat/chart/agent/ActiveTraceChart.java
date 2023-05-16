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
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ActiveTraceChart extends DefaultAgentChart<SampledActiveTrace, Integer> {

    public enum ActiveTraceChartType implements StatChartGroup.AgentChartType {
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

    private static final ChartGroupBuilder<SampledActiveTrace, AgentStatPoint<Integer>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledActiveTrace, AgentStatPoint<Integer>> newChartBuilder() {
        ChartGroupBuilder<SampledActiveTrace, AgentStatPoint<Integer>> builder = new ChartGroupBuilder<>(SampledActiveTrace.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(ActiveTraceChartType.ACTIVE_TRACE_FAST, SampledActiveTrace::getFastCounts);
        builder.addPointFunction(ActiveTraceChartType.ACTIVE_TRACE_NORMAL, SampledActiveTrace::getNormalCounts);
        builder.addPointFunction(ActiveTraceChartType.ACTIVE_TRACE_SLOW, SampledActiveTrace::getSlowCounts);
        builder.addPointFunction(ActiveTraceChartType.ACTIVE_TRACE_VERY_SLOW, SampledActiveTrace::getVerySlowCounts);
        return builder;
    }

    public ActiveTraceChart(TimeWindow timeWindow, List<SampledActiveTrace> statList) {
        super(timeWindow, statList, BUILDER);
    }

}
