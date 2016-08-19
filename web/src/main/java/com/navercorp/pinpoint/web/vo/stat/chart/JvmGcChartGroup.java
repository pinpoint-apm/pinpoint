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

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class JvmGcChartGroup implements AgentStatChartGroup {

    private static final Long UNCOLLECTED_VALUE = -1L;

    private final Map<ChartType, Chart> jvmGcCharts;

    private final String type;

    public enum JvmGcChartType implements ChartType {
        JVM_MEMORY_HEAP_USED,
        JVM_MEMORY_HEAP_MAX,
        JVM_MEMORY_NON_HEAP_USED,
        JVM_MEMORY_NON_HEAP_MAX,
        JVM_GC_OLD_COUNT,
        JVM_GC_OLD_TIME
    }

    public JvmGcChartGroup(TimeWindow timeWindow, List<SampledJvmGc> sampledJvmGcs) {
        this.jvmGcCharts = new HashMap<>();
        JvmGcType jvmGcType = JvmGcType.UNKNOWN;
        List<Point<Long, Long>> heapUseds = new ArrayList<>(sampledJvmGcs.size());
        List<Point<Long, Long>> heapMaxes = new ArrayList<>(sampledJvmGcs.size());
        List<Point<Long, Long>> nonHeapUseds = new ArrayList<>(sampledJvmGcs.size());
        List<Point<Long, Long>> nonHeapMaxes = new ArrayList<>(sampledJvmGcs.size());
        List<Point<Long, Long>> gcOldCounts = new ArrayList<>(sampledJvmGcs.size());
        List<Point<Long, Long>> gcOldTimes = new ArrayList<>(sampledJvmGcs.size());
        for (SampledJvmGc sampledJvmGc : sampledJvmGcs) {
            heapUseds.add(sampledJvmGc.getHeapUsed());
            heapMaxes.add(sampledJvmGc.getHeapMax());
            nonHeapUseds.add(sampledJvmGc.getNonHeapUsed());
            nonHeapMaxes.add(sampledJvmGc.getNonHeapMax());
            gcOldCounts.add(sampledJvmGc.getGcOldCount());
            gcOldTimes.add(sampledJvmGc.getGcOldTime());
            jvmGcType = sampledJvmGc.getJvmGcType();
        }
        jvmGcCharts.put(JvmGcChartType.JVM_MEMORY_HEAP_USED, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(heapUseds));
        jvmGcCharts.put(JvmGcChartType.JVM_MEMORY_HEAP_MAX, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(heapMaxes));
        jvmGcCharts.put(JvmGcChartType.JVM_MEMORY_NON_HEAP_USED, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(nonHeapUseds));
        jvmGcCharts.put(JvmGcChartType.JVM_MEMORY_NON_HEAP_MAX, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(nonHeapMaxes));
        jvmGcCharts.put(JvmGcChartType.JVM_GC_OLD_COUNT, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(gcOldCounts));
        jvmGcCharts.put(JvmGcChartType.JVM_GC_OLD_TIME, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(gcOldTimes));
        this.type = jvmGcType.name();
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return this.jvmGcCharts;
    }

    public String getType() {
        return this.type;
    }
}
