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
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            this.cpuLoadCharts = new HashMap<>();
            List<AgentStatPoint<Double>> jvmCpuLoads = new ArrayList<>(sampledCpuLoads.size());
            List<AgentStatPoint<Double>> systemCpuLoads = new ArrayList<>(sampledCpuLoads.size());
            for (SampledCpuLoad sampledCpuLoad : sampledCpuLoads) {
                jvmCpuLoads.add(sampledCpuLoad.getJvmCpuLoad());
                systemCpuLoads.add(sampledCpuLoad.getSystemCpuLoad());
            }
            TimeSeriesChartBuilder<AgentStatPoint<Double>> chartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledCpuLoad.UNCOLLECTED_POINT_CREATER);
            this.cpuLoadCharts.put(CpuLoadChartType.CPU_LOAD_JVM, chartBuilder.build(jvmCpuLoads));
            this.cpuLoadCharts.put(CpuLoadChartType.CPU_LOAD_SYSTEM, chartBuilder.build(systemCpuLoads));
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
