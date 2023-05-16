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
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class JvmGcChart extends DefaultAgentChart<SampledJvmGc, Long> {

    public enum JvmGcChartType implements StatChartGroup.AgentChartType {
        JVM_MEMORY_HEAP_USED,
        JVM_MEMORY_HEAP_MAX,
        JVM_MEMORY_NON_HEAP_USED,
        JVM_MEMORY_NON_HEAP_MAX,
        JVM_GC_OLD_COUNT,
        JVM_GC_OLD_TIME
    }

    private static final ChartGroupBuilder<SampledJvmGc, AgentStatPoint<Long>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledJvmGc, AgentStatPoint<Long>> newChartBuilder() {
        ChartGroupBuilder<SampledJvmGc, AgentStatPoint<Long>> builder = new ChartGroupBuilder<>(SampledJvmGc.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(JvmGcChartType.JVM_MEMORY_HEAP_USED, SampledJvmGc::getHeapUsed);
        builder.addPointFunction(JvmGcChartType.JVM_MEMORY_HEAP_MAX, SampledJvmGc::getHeapMax);
        builder.addPointFunction(JvmGcChartType.JVM_MEMORY_NON_HEAP_USED, SampledJvmGc::getNonHeapUsed);
        builder.addPointFunction(JvmGcChartType.JVM_MEMORY_NON_HEAP_MAX, SampledJvmGc::getNonHeapMax);
        builder.addPointFunction(JvmGcChartType.JVM_GC_OLD_COUNT, SampledJvmGc::getGcOldCount);
        builder.addPointFunction(JvmGcChartType.JVM_GC_OLD_TIME, SampledJvmGc::getGcOldTime);

        return builder;
    }

    public JvmGcChart(TimeWindow timeWindow, List<SampledJvmGc> statList) {
        super(timeWindow, statList, BUILDER);
    }
}
