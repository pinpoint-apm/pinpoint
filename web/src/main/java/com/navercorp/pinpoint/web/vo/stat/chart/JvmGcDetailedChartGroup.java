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
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class JvmGcDetailedChartGroup implements AgentStatChartGroup{

    private static final Long UNCOLLECTED_VALUE = -1L;
    private static final Double UNCOLLECTED_PERCENTAGE = -1D;

    private final Map<ChartType, Chart> jvmGcDetailedCharts;

    public enum JvmGcDetailedChartType implements ChartType {
        JVM_DETAILED_GC_NEW_COUNT,
        JVM_DETAILED_GC_NEW_TIME,
        JVM_DETAILED_CODE_CACHE_USED,
        JVM_DETAILED_NEW_GEN_USED,
        JVM_DETAILED_OLD_GEN_USED,
        JVM_DETAILED_SURVIVOR_SPACE_USED,
        JVM_DETAILED_PERM_GEN_USED,
        JVM_DETAILED_METASPACE_USED
    }

    public JvmGcDetailedChartGroup(TimeWindow timeWindow, List<SampledJvmGcDetailed> sampledJvmGcDetaileds) {
        this.jvmGcDetailedCharts = new HashMap<>();
        List<Point<Long, Long>> gcNewCounts = new ArrayList<>(sampledJvmGcDetaileds.size());
        List<Point<Long, Long>> gcNewTimes = new ArrayList<>(sampledJvmGcDetaileds.size());
        List<Point<Long, Double>> codeCacheUseds = new ArrayList<>(sampledJvmGcDetaileds.size());
        List<Point<Long, Double>> newGenUseds = new ArrayList<>(sampledJvmGcDetaileds.size());
        List<Point<Long, Double>> oldGenUseds = new ArrayList<>(sampledJvmGcDetaileds.size());
        List<Point<Long, Double>> survivorSpaceUseds = new ArrayList<>(sampledJvmGcDetaileds.size());
        List<Point<Long, Double>> permGenUseds = new ArrayList<>(sampledJvmGcDetaileds.size());
        List<Point<Long, Double>> metaspaceUseds = new ArrayList<>(sampledJvmGcDetaileds.size());
        for (SampledJvmGcDetailed sampledJvmGcDetailed : sampledJvmGcDetaileds) {
            gcNewCounts.add(sampledJvmGcDetailed.getGcNewCount());
            gcNewTimes.add(sampledJvmGcDetailed.getGcNewTime());
            codeCacheUseds.add(sampledJvmGcDetailed.getCodeCacheUsed());
            newGenUseds.add(sampledJvmGcDetailed.getNewGenUsed());
            oldGenUseds.add(sampledJvmGcDetailed.getOldGenUsed());
            survivorSpaceUseds.add(sampledJvmGcDetailed.getSurvivorSpaceUsed());
            permGenUseds.add(sampledJvmGcDetailed.getPermGenUsed());
            metaspaceUseds.add(sampledJvmGcDetailed.getMetaspaceUsed());
        }
        this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_GC_NEW_COUNT, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(gcNewCounts));
        this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_GC_NEW_TIME, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_VALUE).build(gcNewTimes));
        this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_CODE_CACHE_USED, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_PERCENTAGE).build(codeCacheUseds));
        this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_NEW_GEN_USED, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_PERCENTAGE).build(newGenUseds));
        this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_OLD_GEN_USED, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_PERCENTAGE).build(oldGenUseds));
        this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_SURVIVOR_SPACE_USED, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_PERCENTAGE).build(survivorSpaceUseds));
        this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_PERM_GEN_USED, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_PERCENTAGE).build(permGenUseds));
        this.jvmGcDetailedCharts.put(JvmGcDetailedChartType.JVM_DETAILED_METASPACE_USED, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_PERCENTAGE).build(metaspaceUseds));
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return this.jvmGcDetailedCharts;
    }
}
