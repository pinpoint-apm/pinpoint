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
import com.navercorp.pinpoint.web.vo.stat.SampledLoadedClassCount;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LoadedClassCountChart implements StatChart {
    private final LoadedClassCountChartGroup LoadedClassCountChartGroup;

    public LoadedClassCountChart(TimeWindow timeWindow, List<SampledLoadedClassCount> sampledLoadedClassCounts) {
        this.LoadedClassCountChartGroup = new LoadedClassCountChartGroup(timeWindow, sampledLoadedClassCounts);
    }

    @Override
    public StatChartGroup getCharts() {
        return LoadedClassCountChartGroup;
    }

    private static class LoadedClassCountChartGroup implements StatChartGroup {
        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> LoadedClassCountCharts;

        public enum LoadedClassCountChartType implements AgentChartType {
            LOADED_CLASS_COUNT,
            UNLOADED_CLASS_COUNT
        }

        public LoadedClassCountChartGroup(TimeWindow timeWindow, List<SampledLoadedClassCount> sampledLoadedClassCounts) {
            this.timeWindow = timeWindow;
            this.LoadedClassCountCharts = newChart(sampledLoadedClassCounts);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledLoadedClassCount> sampledLoadedClassCounts) {
            Chart<AgentStatPoint<Long>> loadedClassCount = newChart(sampledLoadedClassCounts, SampledLoadedClassCount::getLoadedClassCount);
            Chart<AgentStatPoint<Long>> unloadedClassCount = newChart(sampledLoadedClassCounts, SampledLoadedClassCount::getUnloadedClassCount);

            return ImmutableMap.of(LoadedClassCountChartType.LOADED_CLASS_COUNT, loadedClassCount,
                    LoadedClassCountChartType.UNLOADED_CLASS_COUNT, unloadedClassCount);
        }

        private Chart<AgentStatPoint<Long>> newChart(List<SampledLoadedClassCount> sampledActiveTraces, Function<SampledLoadedClassCount, AgentStatPoint<Long>> function) {
            TimeSeriesChartBuilder<AgentStatPoint<Long>> builder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledLoadedClassCount.UNCOLLECTED_POINT_CREATOR);
            return builder.build(sampledActiveTraces, function);

        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return LoadedClassCountCharts;
        }
    }
}
