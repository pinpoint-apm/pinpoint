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
import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
            this.jvmGcCharts = newChart(sampledJvmGcs);

            JvmGcType jvmGcType = getJvmGcType(sampledJvmGcs);
            this.type = jvmGcType.name();
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledJvmGc> sampledJvmGcs) {
            Chart<AgentStatPoint<Long>> heapUseds = newChart(sampledJvmGcs, SampledJvmGc::getHeapUsed);
            Chart<AgentStatPoint<Long>> heapMaxes = newChart(sampledJvmGcs, SampledJvmGc::getHeapMax);
            Chart<AgentStatPoint<Long>> nonHeapUseds = newChart(sampledJvmGcs, SampledJvmGc::getNonHeapUsed);
            Chart<AgentStatPoint<Long>> nonHeapMaxes = newChart(sampledJvmGcs, SampledJvmGc::getNonHeapMax);
            Chart<AgentStatPoint<Long>> gcOldCounts = newChart(sampledJvmGcs, SampledJvmGc::getGcOldCount);
            Chart<AgentStatPoint<Long>> gcOldTimes = newChart(sampledJvmGcs, SampledJvmGc::getGcOldTime);


            ImmutableMap.Builder<ChartType, Chart<? extends Point>> builder = ImmutableMap.builder();
            builder.put(JvmGcChartType.JVM_MEMORY_HEAP_USED, heapUseds);
            builder.put(JvmGcChartType.JVM_MEMORY_HEAP_MAX, heapMaxes);
            builder.put(JvmGcChartType.JVM_MEMORY_NON_HEAP_USED, nonHeapUseds);
            builder.put(JvmGcChartType.JVM_MEMORY_NON_HEAP_MAX, nonHeapMaxes);
            builder.put(JvmGcChartType.JVM_GC_OLD_COUNT, gcOldCounts);
            builder.put(JvmGcChartType.JVM_GC_OLD_TIME, gcOldTimes);
            return builder.build();
        }

        private Chart<AgentStatPoint<Long>> newChart(List<SampledJvmGc> jvmGcList, Function<SampledJvmGc, AgentStatPoint<Long>> filter) {

            TimeSeriesChartBuilder<AgentStatPoint<Long>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledJvmGc.UNCOLLECTED_POINT_CREATOR);
            return builder.build(jvmGcList, filter);
        }

        private JvmGcType getJvmGcType(List<SampledJvmGc> sampledJvmGcs) {
            if (CollectionUtils.isEmpty(sampledJvmGcs)) {
                return JvmGcType.UNKNOWN;
            } else {
                return ListUtils.getLast(sampledJvmGcs).getJvmGcType();
            }
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
