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
package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.google.common.collect.ImmutableMap;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinLoadedClassBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ApplicationLoadedClassChart implements StatChart {
    private final ApplicationLoadedClassChartGroup loadedClassChartGroup;

    public ApplicationLoadedClassChart(TimeWindow timeWindow, List<AggreJoinLoadedClassBo> aggreJoinLoadedClassBoList) {
        this.loadedClassChartGroup = new ApplicationLoadedClassChartGroup(timeWindow, aggreJoinLoadedClassBoList);
    }

    @Override
    public StatChartGroup getCharts() {
        return loadedClassChartGroup;
    }

    public static class ApplicationLoadedClassChartGroup implements StatChartGroup {
        private static final LongApplicationStatPoint.UncollectedCreator UNCOLLECTED_LOADED_CLASS_POINT = new LongApplicationStatPoint.UncollectedCreator(JoinLoadedClassBo.UNCOLLECTED_VALUE);

        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> loadedClassChartMap;

        public enum LoadedClassChartType implements ApplicationChartType {
            LOADED_CLASS_COUNT,
            UNLOADED_CLASS_COUNT
        }

        public ApplicationLoadedClassChartGroup(TimeWindow timeWindow, List<AggreJoinLoadedClassBo> aggreJoinLoadedClassBoList) {
            this.timeWindow = timeWindow;
            this.loadedClassChartMap = newChart(aggreJoinLoadedClassBoList);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<AggreJoinLoadedClassBo> aggreJoinLoadedClassBoList) {
            Chart<LongApplicationStatPoint> loadedClassCount = newChart(aggreJoinLoadedClassBoList, this::newLoadedClassCount);
            Chart<LongApplicationStatPoint> unloadedClassCount = newChart(aggreJoinLoadedClassBoList, this::newUnloadedClassCount);


            return ImmutableMap.of(LoadedClassChartType.LOADED_CLASS_COUNT, loadedClassCount
                    , LoadedClassChartType.UNLOADED_CLASS_COUNT, unloadedClassCount);
        }

        private Chart<LongApplicationStatPoint> newChart(List<AggreJoinLoadedClassBo> LoadedClassList, Function<AggreJoinLoadedClassBo, LongApplicationStatPoint> filter) {
            TimeSeriesChartBuilder<LongApplicationStatPoint> builder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_LOADED_CLASS_POINT);
            return builder.build(LoadedClassList, filter);
        }

        private LongApplicationStatPoint newLoadedClassCount(AggreJoinLoadedClassBo loadedClassBo) {
            final JoinLongFieldBo loadedClassJoinValue = loadedClassBo.getLoadedClassJoinValue();
            return new LongApplicationStatPoint(loadedClassBo.getTimestamp(),
                    loadedClassJoinValue.getMin(), loadedClassJoinValue.getMinAgentId(),
                    loadedClassJoinValue.getMax(), loadedClassJoinValue.getMaxAgentId(), loadedClassJoinValue.getAvg());
        }

        private LongApplicationStatPoint newUnloadedClassCount(AggreJoinLoadedClassBo loadedClassBo) {
            final JoinLongFieldBo unloadedClassJoinValue = loadedClassBo.getUnloadedClassJoinValue();
            return new LongApplicationStatPoint(loadedClassBo.getTimestamp(),
                    unloadedClassJoinValue.getMin(), unloadedClassJoinValue.getMinAgentId(),
                    unloadedClassJoinValue.getMax(), unloadedClassJoinValue.getMaxAgentId(),
                    unloadedClassJoinValue.getAvg());
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return this.loadedClassChartMap;
        }
    }
}
