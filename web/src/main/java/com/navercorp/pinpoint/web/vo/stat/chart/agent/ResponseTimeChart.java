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
import com.navercorp.pinpoint.web.vo.stat.SampledLoadedClassCount;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class ResponseTimeChart extends DefaultAgentChart<SampledResponseTime, Long> {

    public enum ResponseTimeChartType implements StatChartGroup.AgentChartType {
        AVG,
        MAX
    }

    private static final ChartGroupBuilder<SampledResponseTime, AgentStatPoint<Long>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledResponseTime, AgentStatPoint<Long>> newChartBuilder() {
        ChartGroupBuilder<SampledResponseTime, AgentStatPoint<Long>> builder = new ChartGroupBuilder<>(SampledLoadedClassCount.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(ResponseTimeChartType.AVG, SampledResponseTime::getAvg);
        builder.addPointFunction(ResponseTimeChartType.MAX, SampledResponseTime::getMax);
        return builder;
    }

    public ResponseTimeChart(TimeWindow timeWindow, List<SampledResponseTime> statList) {
        super(timeWindow, statList, BUILDER);
    }

}
