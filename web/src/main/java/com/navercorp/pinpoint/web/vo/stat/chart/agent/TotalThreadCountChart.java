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

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledTotalThreadCount;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class TotalThreadCountChart extends DefaultAgentChart<SampledTotalThreadCount, Long> {

    public enum TotalThreadCountChartType implements StatChartGroup.AgentChartType {
        TOTAL_THREAD_COUNT
    }

    private static final ChartGroupBuilder<SampledTotalThreadCount, AgentStatPoint<Long>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledTotalThreadCount, AgentStatPoint<Long>> newChartBuilder() {
        ChartGroupBuilder<SampledTotalThreadCount, AgentStatPoint<Long>> builder = new ChartGroupBuilder<>(SampledTotalThreadCount.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(TotalThreadCountChartType.TOTAL_THREAD_COUNT, SampledTotalThreadCount::getTotalThreadCount);
        return builder;
    }

    public TotalThreadCountChart(TimeWindow timeWindow, List<SampledTotalThreadCount> statList) {
        super(timeWindow, statList, BUILDER);
    }

}
