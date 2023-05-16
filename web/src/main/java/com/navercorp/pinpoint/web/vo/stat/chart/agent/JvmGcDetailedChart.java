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
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DefaultStatChartGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class JvmGcDetailedChart implements StatChart {

    public enum JvmGcDetailedChartType implements StatChartGroup.AgentChartType {
        JVM_DETAILED_GC_NEW_COUNT,
        JVM_DETAILED_GC_NEW_TIME,
        JVM_DETAILED_CODE_CACHE_USED,
        JVM_DETAILED_NEW_GEN_USED,
        JVM_DETAILED_OLD_GEN_USED,
        JVM_DETAILED_SURVIVOR_SPACE_USED,
        JVM_DETAILED_PERM_GEN_USED,
        JVM_DETAILED_METASPACE_USED
    }

    private static final ChartGroupBuilder<SampledJvmGcDetailed, AgentStatPoint<Long>> LONG_BUILDER = newLongChartBuilder();
    private static final ChartGroupBuilder<SampledJvmGcDetailed, AgentStatPoint<Double>> DOUBLE_BUILDER = newDoubleChartBuilder();

    static ChartGroupBuilder<SampledJvmGcDetailed, AgentStatPoint<Long>> newLongChartBuilder() {
        ChartGroupBuilder<SampledJvmGcDetailed, AgentStatPoint<Long>> builder = new ChartGroupBuilder<>(SampledJvmGcDetailed.UNCOLLECTED_VALUE_POINT_CREATOR);
        builder.addPointFunction(JvmGcDetailedChartType.JVM_DETAILED_GC_NEW_COUNT, SampledJvmGcDetailed::getGcNewCount);
        builder.addPointFunction(JvmGcDetailedChartType.JVM_DETAILED_GC_NEW_TIME, SampledJvmGcDetailed::getGcNewTime);
        return builder;
    }
    static ChartGroupBuilder<SampledJvmGcDetailed, AgentStatPoint<Double>> newDoubleChartBuilder() {
        ChartGroupBuilder<SampledJvmGcDetailed, AgentStatPoint<Double>> builder = new ChartGroupBuilder<>(SampledJvmGcDetailed.UNCOLLECTED_PERCENTAGE_POINT_CREATOR);
        builder.addPointFunction(JvmGcDetailedChartType.JVM_DETAILED_CODE_CACHE_USED, SampledJvmGcDetailed::getCodeCacheUsed);
        builder.addPointFunction(JvmGcDetailedChartType.JVM_DETAILED_NEW_GEN_USED, SampledJvmGcDetailed::getNewGenUsed);
        builder.addPointFunction(JvmGcDetailedChartType.JVM_DETAILED_OLD_GEN_USED, SampledJvmGcDetailed::getOldGenUsed);
        builder.addPointFunction(JvmGcDetailedChartType.JVM_DETAILED_SURVIVOR_SPACE_USED, SampledJvmGcDetailed::getSurvivorSpaceUsed);
        builder.addPointFunction(JvmGcDetailedChartType.JVM_DETAILED_PERM_GEN_USED, SampledJvmGcDetailed::getPermGenUsed);
        builder.addPointFunction(JvmGcDetailedChartType.JVM_DETAILED_METASPACE_USED, SampledJvmGcDetailed::getMetaspaceUsed);
        return builder;
    }

    private final TimeWindow timeWindow;
    private final List<SampledJvmGcDetailed> statList;

    public JvmGcDetailedChart(TimeWindow timeWindow, List<SampledJvmGcDetailed> statList) {
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.statList = Objects.requireNonNull(statList, "statList");
    }

    @Override
    public StatChartGroup getCharts() {
        Map<StatChartGroup.ChartType, Chart<AgentStatPoint<Long>>> longMap = LONG_BUILDER.buildMap(timeWindow, statList);
        Map<StatChartGroup.ChartType, Chart<AgentStatPoint<Double>>> doubleMap = DOUBLE_BUILDER.buildMap(timeWindow, statList);

        Map<StatChartGroup.ChartType, Chart<AgentStatPoint<?>>> merge = new HashMap<>();
        merge.putAll((Map<StatChartGroup.ChartType, Chart<AgentStatPoint<?>>>) (Map<StatChartGroup.ChartType, ?>) longMap);
        merge.putAll((Map<StatChartGroup.ChartType, Chart<AgentStatPoint<?>>>) (Map<StatChartGroup.ChartType, ?>) doubleMap);
        return new DefaultStatChartGroup<>(timeWindow, merge);
    }

}
