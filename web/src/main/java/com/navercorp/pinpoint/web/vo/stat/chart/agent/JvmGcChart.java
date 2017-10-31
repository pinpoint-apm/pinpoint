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

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class JvmGcChart implements StatChart {

    private final JvmGcChartGroup jvmGcChartGroup;

    public JvmGcChart(TimeWindow timeWindow, List<SampledJvmGc> sampledJvmGcs) {
        this.jvmGcChartGroup = new JvmGcChartGroup(timeWindow, sampledJvmGcs);
    }

    @Override
    public StatChartGroup getCharts() {
        return jvmGcChartGroup;
    }

    public String getType() {
        return this.jvmGcChartGroup.getType();
    }

    public static class JvmGcChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private final String type;

        private final Map<ChartType, Chart<? extends Point>> jvmGcCharts;

        public enum JvmGcChartType implements AgentChartType {
            JVM_MEMORY_HEAP_USED,
            JVM_MEMORY_HEAP_MAX,
            JVM_MEMORY_NON_HEAP_USED,
            JVM_MEMORY_NON_HEAP_MAX,
            JVM_GC_OLD_COUNT,
            JVM_GC_OLD_TIME
        }

        public JvmGcChartGroup(TimeWindow timeWindow, List<SampledJvmGc> sampledJvmGcs) {
            this.timeWindow = timeWindow;
            this.jvmGcCharts = new HashMap<>();
            JvmGcType jvmGcType = JvmGcType.UNKNOWN;
            List<AgentStatPoint<Long>> heapUseds = new ArrayList<>(sampledJvmGcs.size());
            List<AgentStatPoint<Long>> heapMaxes = new ArrayList<>(sampledJvmGcs.size());
            List<AgentStatPoint<Long>> nonHeapUseds = new ArrayList<>(sampledJvmGcs.size());
            List<AgentStatPoint<Long>> nonHeapMaxes = new ArrayList<>(sampledJvmGcs.size());
            List<AgentStatPoint<Long>> gcOldCounts = new ArrayList<>(sampledJvmGcs.size());
            List<AgentStatPoint<Long>> gcOldTimes = new ArrayList<>(sampledJvmGcs.size());
            for (SampledJvmGc sampledJvmGc : sampledJvmGcs) {
                heapUseds.add(sampledJvmGc.getHeapUsed());
                heapMaxes.add(sampledJvmGc.getHeapMax());
                nonHeapUseds.add(sampledJvmGc.getNonHeapUsed());
                nonHeapMaxes.add(sampledJvmGc.getNonHeapMax());
                gcOldCounts.add(sampledJvmGc.getGcOldCount());
                gcOldTimes.add(sampledJvmGc.getGcOldTime());
                jvmGcType = sampledJvmGc.getJvmGcType();
            }
            TimeSeriesChartBuilder<AgentStatPoint<Long>> chartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledJvmGc.UNCOLLECTED_POINT_CREATER);
            jvmGcCharts.put(JvmGcChartType.JVM_MEMORY_HEAP_USED, chartBuilder.build(heapUseds));
            jvmGcCharts.put(JvmGcChartType.JVM_MEMORY_HEAP_MAX, chartBuilder.build(heapMaxes));
            jvmGcCharts.put(JvmGcChartType.JVM_MEMORY_NON_HEAP_USED, chartBuilder.build(nonHeapUseds));
            jvmGcCharts.put(JvmGcChartType.JVM_MEMORY_NON_HEAP_MAX, chartBuilder.build(nonHeapMaxes));
            jvmGcCharts.put(JvmGcChartType.JVM_GC_OLD_COUNT, chartBuilder.build(gcOldCounts));
            jvmGcCharts.put(JvmGcChartType.JVM_GC_OLD_TIME, chartBuilder.build(gcOldTimes));
            this.type = jvmGcType.name();
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return jvmGcCharts;
        }

        public String getType() {
            return type;
        }
    }
}
