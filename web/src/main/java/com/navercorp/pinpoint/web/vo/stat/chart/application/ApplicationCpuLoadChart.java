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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinCpuLoadBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

        private static final DoubleApplicationStatPoint.UncollectedCreator UNCOLLECTED_CPULOAD_POINT = new DoubleApplicationStatPoint.UncollectedCreator(JoinCpuLoadBo.UNCOLLECTED_VALUE);

        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> cpuLoadChartMap;

        public enum CpuLoadChartType implements ApplicationChartType {
            CPU_LOAD_JVM,
            CPU_LOAD_SYSTEM
        }

        public ApplicationCpuLoadChartGroup(TimeWindow timeWindow, List<AggreJoinCpuLoadBo> aggreCpuLoadList) {
            this.timeWindow = timeWindow;
            this.cpuLoadChartMap = newChart(aggreCpuLoadList);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<AggreJoinCpuLoadBo> aggreCpuLoadList) {
            Chart<DoubleApplicationStatPoint> jvmCpuLoadChart = newChart(aggreCpuLoadList, this::newJvmCpu);
            Chart<DoubleApplicationStatPoint> systemCpuLoadChart = newChart(aggreCpuLoadList, this::newSystemCpu);
            return ImmutableMap.of(CpuLoadChartType.CPU_LOAD_JVM, jvmCpuLoadChart, CpuLoadChartType.CPU_LOAD_SYSTEM, systemCpuLoadChart);
        }

        private Chart<DoubleApplicationStatPoint> newChart(List<AggreJoinCpuLoadBo> cpuLoadList, Function<AggreJoinCpuLoadBo, DoubleApplicationStatPoint> filter) {

            TimeSeriesChartBuilder<DoubleApplicationStatPoint> builder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_CPULOAD_POINT);
            return builder.build(cpuLoadList, filter);
        }

        private DoubleApplicationStatPoint newSystemCpu(AggreJoinCpuLoadBo cpuLoad) {
            final JoinDoubleFieldBo systemCpuLoadJoinValue = cpuLoad.getSystemCpuLoadJoinValue();
            return new DoubleApplicationStatPoint(cpuLoad.getTimestamp(), systemCpuLoadJoinValue.getMin(), systemCpuLoadJoinValue.getMinAgentId(),
                    systemCpuLoadJoinValue.getMax(), systemCpuLoadJoinValue.getMaxAgentId(), systemCpuLoadJoinValue.getAvg());
        }

        private DoubleApplicationStatPoint newJvmCpu(AggreJoinCpuLoadBo cpuLoad) {
            final JoinDoubleFieldBo jvmCpuLoadJoinValue = cpuLoad.getJvmCpuLoadJoinValue();
            return new DoubleApplicationStatPoint(cpuLoad.getTimestamp(), jvmCpuLoadJoinValue.getMin(), jvmCpuLoadJoinValue.getMinAgentId(),
                    jvmCpuLoadJoinValue.getMax(), jvmCpuLoadJoinValue.getMaxAgentId(), jvmCpuLoadJoinValue.getAvg());
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
