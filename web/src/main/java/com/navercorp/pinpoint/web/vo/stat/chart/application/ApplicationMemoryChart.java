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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class ApplicationMemoryChart extends DefaultApplicationChart<AggreJoinMemoryBo, Double> {

    private static final Point.UncollectedPointCreator<ApplicationStatPoint<Double>> UNCOLLECTED_POINT
            = new DoubleApplicationStatPoint.UncollectedCreator(JoinMemoryBo.UNCOLLECTED_VALUE);

    private static final ChartGroupBuilder<AggreJoinMemoryBo, ApplicationStatPoint<Double>> BUILDER = newChartBuilder();

    public enum MemoryChartType implements StatChartGroup.ApplicationChartType {
        MEMORY_HEAP,
        MEMORY_NON_HEAP
    }

    static ChartGroupBuilder<AggreJoinMemoryBo, ApplicationStatPoint<Double>> newChartBuilder() {
        ChartGroupBuilder<AggreJoinMemoryBo, ApplicationStatPoint<Double>> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(MemoryChartType.MEMORY_HEAP, ApplicationMemoryChart::newHeap);
        builder.addPointFunction(MemoryChartType.MEMORY_NON_HEAP, ApplicationMemoryChart::newNonHeap);
        return builder;
    }


    public ApplicationMemoryChart(TimeWindow timeWindow, List<AggreJoinMemoryBo> appStatList) {
        super(timeWindow, appStatList, BUILDER);
    }

    private static ApplicationStatPoint<Double> newHeap(AggreJoinMemoryBo memory) {
        final JoinLongFieldBo heapUsedJoinValue = memory.getHeapUsedJoinValue();
        long timestamp = memory.getTimestamp();
        return StatPointUtils.longToDoubleStatPoint(timestamp, heapUsedJoinValue);
    }

    private static ApplicationStatPoint<Double> newNonHeap(AggreJoinMemoryBo memory) {
        final JoinLongFieldBo nonHeapUsedJoinValue = memory.getNonHeapUsedJoinValue();
        long timestamp = memory.getTimestamp();
        return StatPointUtils.longToDoubleStatPoint(timestamp, nonHeapUsedJoinValue);
    }

}
