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
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class CpuLoadChartGroup implements AgentStatChartGroup {

    private static final Double UNCOLLECTED_PERCENTAGE = -1D;

    private final Map<ChartType, Chart> cpuLoadCharts;

    public enum CpuLoadChartType implements ChartType {
        CPU_LOAD_JVM,
        CPU_LOAD_SYSTEM
    }

    public CpuLoadChartGroup(TimeWindow timeWindow, List<SampledCpuLoad> sampledCpuLoads) {
        this.cpuLoadCharts = new HashMap<>();
        List<Point<Long, Double>> jvmCpuLoads = new ArrayList<>(sampledCpuLoads.size());
        List<Point<Long, Double>> systemCpuLoads = new ArrayList<>(sampledCpuLoads.size());
        for (SampledCpuLoad sampledCpuLoad : sampledCpuLoads) {
            jvmCpuLoads.add(sampledCpuLoad.getJvmCpuLoad());
            systemCpuLoads.add(sampledCpuLoad.getSystemCpuLoad());
        }
        this.cpuLoadCharts.put(CpuLoadChartType.CPU_LOAD_JVM, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_PERCENTAGE).build(jvmCpuLoads));
        this.cpuLoadCharts.put(CpuLoadChartType.CPU_LOAD_SYSTEM, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_PERCENTAGE).build(systemCpuLoads));
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return this.cpuLoadCharts;
    }
}
