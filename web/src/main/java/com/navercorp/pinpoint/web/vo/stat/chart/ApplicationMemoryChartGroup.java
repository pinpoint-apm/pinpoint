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
package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;
import com.navercorp.pinpoint.web.vo.stat.chart.MemoryPoint.UncollectedMemoryPointCreater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class ApplicationMemoryChartGroup implements ApplicationStatChartGroup {

    public static final UncollectedMemoryPointCreater UNCOLLECTED_MEMORYPOINT = new UncollectedMemoryPointCreater();
    private final Map<ChartType, Chart> memoryChartMap;

    public enum MemoryChartType implements ChartType {
        MEMORY_HEAP,
        MEMORY_NON_HEAP
    }

    public ApplicationMemoryChartGroup(TimeWindow timeWindow, List<AggreJoinMemoryBo> aggreJoinMemoryBoList) {
        memoryChartMap = new HashMap<>();
        List<Point> heapList = new ArrayList<>(aggreJoinMemoryBoList.size());
        List<Point> nonHeapList = new ArrayList<>(aggreJoinMemoryBoList.size());

        for (AggreJoinMemoryBo aggreJoinMemoryBo : aggreJoinMemoryBoList) {
            heapList.add(new MemoryPoint(aggreJoinMemoryBo.getTimestamp(), aggreJoinMemoryBo.getMinHeapUsed(), aggreJoinMemoryBo.getMinHeapAgentId(), aggreJoinMemoryBo.getMaxHeapUsed(), aggreJoinMemoryBo.getMaxHeapAgentId(), aggreJoinMemoryBo.getHeapUsed()));
            nonHeapList.add(new MemoryPoint(aggreJoinMemoryBo.getTimestamp(), aggreJoinMemoryBo.getMinNonHeapUsed(), aggreJoinMemoryBo.getMinNonHeapAgentId(), aggreJoinMemoryBo.getMaxNonHeapUsed(), aggreJoinMemoryBo.getMaxNonHeapAgentId(), aggreJoinMemoryBo.getNonHeapUsed()));
        }

        memoryChartMap.put(MemoryChartType.MEMORY_HEAP, new TimeSeriesChartBuilder(timeWindow, UNCOLLECTED_MEMORYPOINT).build(heapList));
        memoryChartMap.put(MemoryChartType.MEMORY_NON_HEAP, new TimeSeriesChartBuilder(timeWindow, UNCOLLECTED_MEMORYPOINT).build(nonHeapList));
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return this.memoryChartMap;
    }
}
