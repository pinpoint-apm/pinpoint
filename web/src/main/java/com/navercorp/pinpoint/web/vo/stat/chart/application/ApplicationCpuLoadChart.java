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
package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinCpuLoadBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class ApplicationCpuLoadChart implements StatChart {

    private final ApplicationCpuLoadChartGroup cpuLoadChartGroup;

    public ApplicationCpuLoadChart(TimeWindow timeWindow, List<AggreJoinCpuLoadBo> aggreJoinCpuLoadBoList) {
        this.cpuLoadChartGroup = new ApplicationCpuLoadChartGroup(timeWindow, aggreJoinCpuLoadBoList);
    }

    @Override
    public StatChartGroup getCharts() {
        return cpuLoadChartGroup;
    }

    public static class ApplicationCpuLoadChartGroup implements StatChartGroup {

        private static final CpuLoadPoint.UncollectedCpuLoadPointCreator UNCOLLECTED_CPULOAD_POINT = new CpuLoadPoint.UncollectedCpuLoadPointCreator();

        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> cpuLoadChartMap;

        public enum CpuLoadChartType implements ApplicationChartType {
            CPU_LOAD_JVM,
            CPU_LOAD_SYSTEM
        }

        public ApplicationCpuLoadChartGroup(TimeWindow timeWindow, List<AggreJoinCpuLoadBo> aggreCpuLoadList) {
            this.timeWindow = timeWindow;
            cpuLoadChartMap = new HashMap<>();
            List<CpuLoadPoint> jvmCpuLoadList = new ArrayList<>(aggreCpuLoadList.size());
            List<CpuLoadPoint> systemCpuLoadList = new ArrayList<>(aggreCpuLoadList.size());

            for (AggreJoinCpuLoadBo aggreJoinCpuLoadBo : aggreCpuLoadList) {
                jvmCpuLoadList.add(new CpuLoadPoint(aggreJoinCpuLoadBo.getTimestamp(), aggreJoinCpuLoadBo.getMinJvmCpuLoad(), aggreJoinCpuLoadBo.getMinJvmCpuAgentId(), aggreJoinCpuLoadBo.getMaxJvmCpuLoad(), aggreJoinCpuLoadBo.getMaxJvmCpuAgentId(), aggreJoinCpuLoadBo.getJvmCpuLoad()));
                systemCpuLoadList.add(new CpuLoadPoint(aggreJoinCpuLoadBo.getTimestamp(), aggreJoinCpuLoadBo.getMinSystemCpuLoad(), aggreJoinCpuLoadBo.getMinSysCpuAgentId(), aggreJoinCpuLoadBo.getMaxSystemCpuLoad(), aggreJoinCpuLoadBo.getMaxSysCpuAgentId(), aggreJoinCpuLoadBo.getSystemCpuLoad()));
            }
            TimeSeriesChartBuilder<CpuLoadPoint> chartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_CPULOAD_POINT);
            cpuLoadChartMap.put(CpuLoadChartType.CPU_LOAD_JVM, chartBuilder.build(jvmCpuLoadList));
            cpuLoadChartMap.put(CpuLoadChartType.CPU_LOAD_SYSTEM, chartBuilder.build(systemCpuLoadList));
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return this.cpuLoadChartMap;
        }
    }
}
