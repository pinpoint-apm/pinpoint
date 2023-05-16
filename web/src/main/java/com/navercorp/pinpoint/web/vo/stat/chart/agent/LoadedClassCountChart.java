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

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledLoadedClassCount;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class LoadedClassCountChart extends DefaultAgentChart<SampledLoadedClassCount, Long> {

    public enum LoadedClassCountChartType implements StatChartGroup.AgentChartType {
        LOADED_CLASS_COUNT,
        UNLOADED_CLASS_COUNT
    }

    private static final ChartGroupBuilder<SampledLoadedClassCount, AgentStatPoint<Long>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledLoadedClassCount, AgentStatPoint<Long>> newChartBuilder() {
        ChartGroupBuilder<SampledLoadedClassCount, AgentStatPoint<Long>> builder = new ChartGroupBuilder<>(SampledLoadedClassCount.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(LoadedClassCountChartType.LOADED_CLASS_COUNT, SampledLoadedClassCount::getLoadedClassCount);
        builder.addPointFunction(LoadedClassCountChartType.UNLOADED_CLASS_COUNT, SampledLoadedClassCount::getUnloadedClassCount);
        return builder;
    }

    public LoadedClassCountChart(TimeWindow timeWindow, List<SampledLoadedClassCount> statList) {
        super(timeWindow, statList, BUILDER);
    }

}
