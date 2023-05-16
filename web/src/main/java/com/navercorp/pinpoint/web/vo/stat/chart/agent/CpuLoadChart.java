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
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class CpuLoadChart extends DefaultAgentChart<SampledCpuLoad, Double> {

    public enum CpuLoadChartType implements StatChartGroup.AgentChartType {
        CPU_LOAD_JVM,
        CPU_LOAD_SYSTEM
    }

    private static final ChartGroupBuilder<SampledCpuLoad, AgentStatPoint<Double>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledCpuLoad, AgentStatPoint<Double>> newChartBuilder() {
        ChartGroupBuilder<SampledCpuLoad, AgentStatPoint<Double>> builder = new ChartGroupBuilder<>(SampledCpuLoad.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(CpuLoadChartType.CPU_LOAD_JVM, SampledCpuLoad::getJvmCpuLoad);
        builder.addPointFunction(CpuLoadChartType.CPU_LOAD_SYSTEM, SampledCpuLoad::getSystemCpuLoad);
        return builder;
    }

    public CpuLoadChart(TimeWindow timeWindow, List<SampledCpuLoad> statList) {
        super(timeWindow, statList, BUILDER);
    }
}
