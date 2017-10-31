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
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class JvmGcDetailedChart implements StatChart {

    private final JvmGcDetailedChartGroup jvmGcDetailedChartGroup;

    public JvmGcDetailedChart(TimeWindow timeWindow, List<SampledJvmGcDetailed> sampledJvmGcDetaileds) {
        this.jvmGcDetailedChartGroup = new JvmGcDetailedChartGroup(timeWindow, sampledJvmGcDetaileds);
    }

    @Override
    public StatChartGroup getCharts() {
        return jvmGcDetailedChartGroup;
    }

    public static class JvmGcDetailedChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private final Map<ChartType, Chart<? extends Point>> jvmGcDetailedCharts;

        public enum JvmGcDetailedChartType implements AgentChartType {
            JVM_DETAILED_GC_NEW_COUNT,
            JVM_DETAILED_GC_NEW_TIME,
            JVM_DETAILED_CODE_CACHE_USED,
            JVM_DETAILED_NEW_GEN_USED,
            JVM_DETAILED_OLD_GEN_USED,
            JVM_DETAILED_SURVIVOR_SPACE_USED,
            JVM_DETAILED_PERM_GEN_USED,
            JVM_DETAILED_METASPACE_USED
        }

        public JvmGcDetailedChartGroup(TimeWindow timeWindow, List<SampledJvmGcDetailed> sampledJvmGcDetailedList) {
            this.timeWindow = timeWindow;
            this.jvmGcDetailedCharts = new HashMap<>();
            List<AgentStatPoint<Long>> gcNewCounts = new ArrayList<>(sampledJvmGcDetailedList.size());
            List<AgentStatPoint<Long>> gcNewTimes = new ArrayList<>(sampledJvmGcDetailedList.size());
            List<AgentStatPoint<Double>> codeCacheUseds = new ArrayList<>(sampledJvmGcDetailedList.size());
            List<AgentStatPoint<Double>> newGenUseds = new ArrayList<>(sampledJvmGcDetailedList.size());
            List<AgentStatPoint<Double>> oldGenUseds = new ArrayList<>(sampledJvmGcDetailedList.size());
            List<AgentStatPoint<Double>> survivorSpaceUseds = new ArrayList<>(sampledJvmGcDetailedList.size());
            List<AgentStatPoint<Double>> permGenUseds = new ArrayList<>(sampledJvmGcDetailedList.size());
            List<AgentStatPoint<Double>> metaspaceUseds = new ArrayList<>(sampledJvmGcDetailedList.size());
            for (SampledJvmGcDetailed sampledJvmGcDetailed : sampledJvmGcDetailedList) {
                gcNewCounts.add(sampledJvmGcDetailed.getGcNewCount());
                gcNewTimes.add(sampledJvmGcDetailed.getGcNewTime());
                codeCacheUseds.add(sampledJvmGcDetailed.getCodeCacheUsed());
                newGenUseds.add(sampledJvmGcDetailed.getNewGenUsed());
                oldGenUseds.add(sampledJvmGcDetailed.getOldGenUsed());
                survivorSpaceUseds.add(sampledJvmGcDetailed.getSurvivorSpaceUsed());
                permGenUseds.add(sampledJvmGcDetailed.getPermGenUsed());
                metaspaceUseds.add(sampledJvmGcDetailed.getMetaspaceUsed());
            }
            TimeSeriesChartBuilder<AgentStatPoint<Long>> valueChartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledJvmGcDetailed.UNCOLLECTED_VALUE_POINT_CREATER);
            TimeSeriesChartBuilder<AgentStatPoint<Double>> percentageChartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledJvmGcDetailed.UNCOLLECTED_PERCENTAGE_POINT_CREATOR);
            this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_GC_NEW_COUNT, valueChartBuilder.build(gcNewCounts));
            this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_GC_NEW_TIME, valueChartBuilder.build(gcNewTimes));
            this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_CODE_CACHE_USED, percentageChartBuilder.build(codeCacheUseds));
            this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_NEW_GEN_USED, percentageChartBuilder.build(newGenUseds));
            this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_OLD_GEN_USED, percentageChartBuilder.build(oldGenUseds));
            this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_SURVIVOR_SPACE_USED, percentageChartBuilder.build(survivorSpaceUseds));
            this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_PERM_GEN_USED, percentageChartBuilder.build(permGenUseds));
            this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_METASPACE_USED, percentageChartBuilder.build(metaspaceUseds));
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return jvmGcDetailedCharts;
        }
    }
}
