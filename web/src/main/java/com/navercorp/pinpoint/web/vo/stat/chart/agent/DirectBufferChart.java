/*
 * Copyright 2018 NAVER Corp.
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
import com.navercorp.pinpoint.web.vo.stat.SampledDirectBuffer;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author Roy Kim
 */
public class DirectBufferChart extends DefaultAgentChart<SampledDirectBuffer, Long> {

    public enum DirectBufferChartType implements StatChartGroup.AgentChartType {
        DIRECT_COUNT,
        DIRECT_MEMORY_USED,
        MAPPED_COUNT,
        MAPPED_MEMORY_USED
    }

    private static final ChartGroupBuilder<SampledDirectBuffer, AgentStatPoint<Long>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledDirectBuffer, AgentStatPoint<Long>> newChartBuilder() {
        ChartGroupBuilder<SampledDirectBuffer, AgentStatPoint<Long>> builder = new ChartGroupBuilder<>(SampledDirectBuffer.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(DirectBufferChartType.DIRECT_COUNT, SampledDirectBuffer::getDirectCount);
        builder.addPointFunction(DirectBufferChartType.DIRECT_MEMORY_USED, SampledDirectBuffer::getDirectMemoryUsed);
        builder.addPointFunction(DirectBufferChartType.MAPPED_COUNT, SampledDirectBuffer::getMappedCount);
        builder.addPointFunction(DirectBufferChartType.MAPPED_MEMORY_USED, SampledDirectBuffer::getMappedMemoryUsed);
        return builder;
    }

    public DirectBufferChart(TimeWindow timeWindow, List<SampledDirectBuffer> statList) {
        super(timeWindow, statList, BUILDER);
    }

}
