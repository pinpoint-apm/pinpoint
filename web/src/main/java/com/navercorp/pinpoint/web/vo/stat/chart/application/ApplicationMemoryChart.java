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

package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class ApplicationMemoryChart implements StatChart {

    private final ApplicationMemoryChartGroup applicationMemoryChartGroup;

    public ApplicationMemoryChart(TimeWindow timeWindow, List<AggreJoinMemoryBo> aggreJoinMemoryBos) {
        this.applicationMemoryChartGroup = new ApplicationMemoryChartGroup(timeWindow, aggreJoinMemoryBos);
    }

    @Override
    public StatChartGroup getCharts() {
        return applicationMemoryChartGroup;
    }

    public static class ApplicationMemoryChartGroup implements StatChartGroup {

        private static final MemoryPoint.UncollectedMemoryPointCreator UNCOLLECTED_MEMORY_POINT = new MemoryPoint.UncollectedMemoryPointCreator();
        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> memoryChartMap;

        public enum MemoryChartType implements ApplicationChartType {
            MEMORY_HEAP,
            MEMORY_NON_HEAP
        }

        public ApplicationMemoryChartGroup(TimeWindow timeWindow, List<AggreJoinMemoryBo> aggreJoinMemoryBoList) {
            this.timeWindow = timeWindow;
            memoryChartMap = new HashMap<>();
            List<MemoryPoint> heapList = new ArrayList<>(aggreJoinMemoryBoList.size());
            List<MemoryPoint> nonHeapList = new ArrayList<>(aggreJoinMemoryBoList.size());

            for (AggreJoinMemoryBo aggreJoinMemoryBo : aggreJoinMemoryBoList) {
                heapList.add(new MemoryPoint(aggreJoinMemoryBo.getTimestamp(), aggreJoinMemoryBo.getMinHeapUsed(), aggreJoinMemoryBo.getMinHeapAgentId(), aggreJoinMemoryBo.getMaxHeapUsed(), aggreJoinMemoryBo.getMaxHeapAgentId(), aggreJoinMemoryBo.getHeapUsed()));
                nonHeapList.add(new MemoryPoint(aggreJoinMemoryBo.getTimestamp(), aggreJoinMemoryBo.getMinNonHeapUsed(), aggreJoinMemoryBo.getMinNonHeapAgentId(), aggreJoinMemoryBo.getMaxNonHeapUsed(), aggreJoinMemoryBo.getMaxNonHeapAgentId(), aggreJoinMemoryBo.getNonHeapUsed()));
            }
            TimeSeriesChartBuilder<MemoryPoint> chartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_MEMORY_POINT);
            memoryChartMap.put(MemoryChartType.MEMORY_HEAP, chartBuilder.build(heapList));
            memoryChartMap.put(MemoryChartType.MEMORY_NON_HEAP, chartBuilder.build(nonHeapList));
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return this.memoryChartMap;
        }
    }
}
