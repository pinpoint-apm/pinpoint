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

import com.google.common.collect.ImmutableMap;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledDirectBuffer;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Roy Kim
 */
public class DirectBufferChart implements StatChart {

    private final DirectBufferChartGroup DirectBufferChartGroup;

    public DirectBufferChart(TimeWindow timeWindow, List<SampledDirectBuffer> sampledDirectBuffers) {
        this.DirectBufferChartGroup = new DirectBufferChartGroup(timeWindow, sampledDirectBuffers);
    }

    @Override
    public StatChartGroup getCharts() {
        return DirectBufferChartGroup;
    }

    public static class DirectBufferChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private Map<ChartType, Chart<? extends Point>> DirectBufferCharts;

        public enum DirectBufferChartType implements AgentChartType {
            DIRECT_COUNT,
            DIRECT_MEMORY_USED,
            MAPPED_COUNT,
            MAPPED_MEMORY_USED
        }

        private DirectBufferChartGroup(TimeWindow timeWindow, List<SampledDirectBuffer> sampledDirectBuffers) {
            this.timeWindow = timeWindow;
            this.DirectBufferCharts = newChart(sampledDirectBuffers);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledDirectBuffer> sampledDirectBuffers) {
            Chart<AgentStatPoint<Long>> directCount = newChart(sampledDirectBuffers, SampledDirectBuffer::getDirectCount);
            Chart<AgentStatPoint<Long>> directMemoryUsed = newChart(sampledDirectBuffers, SampledDirectBuffer::getDirectMemoryUsed);
            Chart<AgentStatPoint<Long>> mappedCount = newChart(sampledDirectBuffers, SampledDirectBuffer::getMappedCount);
            Chart<AgentStatPoint<Long>> mappedMemoryUsed = newChart(sampledDirectBuffers, SampledDirectBuffer::getMappedMemoryUsed);

            return ImmutableMap.of(DirectBufferChartType.DIRECT_COUNT, directCount,
                    DirectBufferChartType.DIRECT_MEMORY_USED, directMemoryUsed,
                    DirectBufferChartType.MAPPED_COUNT, mappedCount,
                    DirectBufferChartType.MAPPED_MEMORY_USED, mappedMemoryUsed);
        }

        private Chart<AgentStatPoint<Long>> newChart(List<SampledDirectBuffer> sampledActiveTraces, Function<SampledDirectBuffer, AgentStatPoint<Long>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Long>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledDirectBuffer.UNCOLLECTED_POINT_CREATOR);
            return builder.build(sampledActiveTraces, function);
        }



        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return DirectBufferCharts;
        }
    }
}
