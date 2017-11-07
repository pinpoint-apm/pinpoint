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
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
            this.jvmGcDetailedCharts = newChart(sampledJvmGcDetailedList);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledJvmGcDetailed> gcDetailedList) {
            // TODO Refactor generic cast
            Chart<AgentStatPoint<Long>> gcNewCounts = newLongChart(gcDetailedList, SampledJvmGcDetailed::getGcNewCount);
            Chart<AgentStatPoint<Long>> gcNewTimes = newLongChart(gcDetailedList, SampledJvmGcDetailed::getGcNewTime);
            Chart<AgentStatPoint<Double>> codeCacheUseds = newDoubleChart(gcDetailedList, SampledJvmGcDetailed::getCodeCacheUsed);
            Chart<AgentStatPoint<Double>> newGenUseds = newDoubleChart(gcDetailedList, SampledJvmGcDetailed::getNewGenUsed);
            Chart<AgentStatPoint<Double>> oldGenUseds = newDoubleChart(gcDetailedList, SampledJvmGcDetailed::getOldGenUsed);
            Chart<AgentStatPoint<Double>> survivorSpaceUseds = newDoubleChart(gcDetailedList, SampledJvmGcDetailed::getSurvivorSpaceUsed);
            Chart<AgentStatPoint<Double>> permGenUseds = newDoubleChart(gcDetailedList, SampledJvmGcDetailed::getPermGenUsed);
            Chart<AgentStatPoint<Double>> metaspaceUseds = newDoubleChart(gcDetailedList, SampledJvmGcDetailed::getMetaspaceUsed);

            ImmutableMap.Builder<ChartType, Chart<? extends Point>> builder = ImmutableMap.builder();
            builder.put(JvmGcDetailedChartType.JVM_DETAILED_GC_NEW_COUNT, gcNewCounts);
            builder.put(JvmGcDetailedChartType.JVM_DETAILED_GC_NEW_TIME, gcNewTimes);
            builder.put(JvmGcDetailedChartType.JVM_DETAILED_CODE_CACHE_USED, codeCacheUseds);
            builder.put(JvmGcDetailedChartType.JVM_DETAILED_NEW_GEN_USED, newGenUseds);
            builder.put(JvmGcDetailedChartType.JVM_DETAILED_OLD_GEN_USED, oldGenUseds);
            builder.put(JvmGcDetailedChartType.JVM_DETAILED_SURVIVOR_SPACE_USED, survivorSpaceUseds);
            builder.put(JvmGcDetailedChartType.JVM_DETAILED_PERM_GEN_USED, permGenUseds);
            builder.put(JvmGcDetailedChartType.JVM_DETAILED_METASPACE_USED, metaspaceUseds);
            return builder.build();
        }

        private Chart<AgentStatPoint<Double>> newDoubleChart(List<SampledJvmGcDetailed> sampledDataSourceList, Function<SampledJvmGcDetailed, AgentStatPoint<Double>> filter) {
            TimeSeriesChartBuilder<AgentStatPoint<Double>> builder = new TimeSeriesChartBuilder<>(timeWindow, SampledJvmGcDetailed.UNCOLLECTED_PERCENTAGE_POINT_CREATOR);
            return builder.build(sampledDataSourceList, filter);
        }

        private Chart<AgentStatPoint<Long>> newLongChart(List<SampledJvmGcDetailed> sampledDataSourceList, Function<SampledJvmGcDetailed, AgentStatPoint<Long>> filter) {
            TimeSeriesChartBuilder<AgentStatPoint<Long>> builder = new TimeSeriesChartBuilder<>(timeWindow, SampledJvmGcDetailed.UNCOLLECTED_VALUE_POINT_CREATOR);
            return builder.build(sampledDataSourceList, filter);
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
