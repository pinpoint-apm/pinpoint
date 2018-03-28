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
import com.navercorp.pinpoint.web.vo.stat.SampledFileDescriptor;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Roy Kim
 */
public class FileDescriptorChart implements StatChart {

    private final FileDescriptorChartGroup FileDescriptorChartGroup;

    public FileDescriptorChart(TimeWindow timeWindow, List<SampledFileDescriptor> sampledFileDescriptors) {
        this.FileDescriptorChartGroup = new FileDescriptorChartGroup(timeWindow, sampledFileDescriptors);
    }

    @Override
    public StatChartGroup getCharts() {
        return FileDescriptorChartGroup;
    }

    public static class FileDescriptorChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private Map<ChartType, Chart<? extends Point>> FileDescriptorCharts;

        public enum FileDescriptorChartType implements AgentChartType {
            OPEN_FILE_DESCRIPTOR_COUNT
        }

        private FileDescriptorChartGroup(TimeWindow timeWindow, List<SampledFileDescriptor> sampledFileDescriptors) {
            this.timeWindow = timeWindow;
            this.FileDescriptorCharts = newChart(sampledFileDescriptors);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledFileDescriptor> sampledFileDescriptors) {
            Chart<AgentStatPoint<Long>> openFileDescriptorChart = newChart(sampledFileDescriptors, SampledFileDescriptor::getOpenFileDescriptorCount);

            return ImmutableMap.of(FileDescriptorChartType.OPEN_FILE_DESCRIPTOR_COUNT, openFileDescriptorChart);
        }

        private Chart<AgentStatPoint<Long>> newChart(List<SampledFileDescriptor> sampledActiveTraces, Function<SampledFileDescriptor, AgentStatPoint<Long>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Long>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledFileDescriptor.UNCOLLECTED_POINT_CREATOR);
            return builder.build(sampledActiveTraces, function);
        }



        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return FileDescriptorCharts;
        }
    }
}
