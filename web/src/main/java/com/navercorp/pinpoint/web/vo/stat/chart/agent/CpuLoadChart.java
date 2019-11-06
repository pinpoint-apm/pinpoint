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

import com.google.common.collect.ImmutableMap;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author HyunGil Jeong
 */
public class CpuLoadChart implements StatChart {

    private final CpuLoadChartGroup cpuLoadChartGroup;

    public CpuLoadChart(TimeWindow timeWindow, List<SampledCpuLoad> sampledCpuLoads) {
        this.cpuLoadChartGroup = new CpuLoadChartGroup(timeWindow, sampledCpuLoads);
    }

    @Override
    public StatChartGroup getCharts() {
        return cpuLoadChartGroup;
    }

    public static class CpuLoadChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private Map<ChartType, Chart<? extends Point>> cpuLoadCharts;

        public enum CpuLoadChartType implements AgentChartType {
            CPU_LOAD_JVM,
            CPU_LOAD_SYSTEM
        }

        private CpuLoadChartGroup(TimeWindow timeWindow, List<SampledCpuLoad> sampledCpuLoads) {
            this.timeWindow = timeWindow;
            this.cpuLoadCharts = newChart(sampledCpuLoads);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledCpuLoad> sampledCpuLoads) {
            Chart<AgentStatPoint<Double>> jvmCpuLoadChart = newChart(sampledCpuLoads, SampledCpuLoad::getJvmCpuLoad);
            Chart<AgentStatPoint<Double>> systemCpuLoadChart = newChart(sampledCpuLoads, SampledCpuLoad::getSystemCpuLoad);

            return ImmutableMap.of(CpuLoadChartType.CPU_LOAD_JVM, jvmCpuLoadChart, CpuLoadChartType.CPU_LOAD_SYSTEM, systemCpuLoadChart);
        }

        private Chart<AgentStatPoint<Double>> newChart(List<SampledCpuLoad> sampledActiveTraces, Function<SampledCpuLoad, AgentStatPoint<Double>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Double>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledCpuLoad.UNCOLLECTED_POINT_CREATOR);
            return builder.build(sampledActiveTraces, function);
        }



        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return cpuLoadCharts;
        }
    }
}
