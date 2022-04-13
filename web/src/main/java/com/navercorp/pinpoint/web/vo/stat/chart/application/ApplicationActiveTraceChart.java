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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinActiveTraceBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;


/**
 * @author minwoo.jung
 */
public class ApplicationActiveTraceChart extends DefaultApplicationChart<AggreJoinActiveTraceBo, Integer> {

    private static final Point.UncollectedPointCreator<ApplicationStatPoint<Integer>> UNCOLLECTED_POINT
            = new IntApplicationStatPoint.UncollectedCreator(JoinActiveTraceBo.UNCOLLECTED_VALUE);

    public enum ActiveTraceChartType implements StatChartGroup.ApplicationChartType {
        ACTIVE_TRACE_COUNT
    }

    private static final ChartGroupBuilder<AggreJoinActiveTraceBo, ApplicationStatPoint<Integer>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<AggreJoinActiveTraceBo, ApplicationStatPoint<Integer>> newChartBuilder() {
        ChartGroupBuilder<AggreJoinActiveTraceBo, ApplicationStatPoint<Integer>> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(ActiveTraceChartType.ACTIVE_TRACE_COUNT, ApplicationActiveTraceChart::newActiveTracePoint);
        return builder;
    }

    public ApplicationActiveTraceChart(TimeWindow timeWindow, List<AggreJoinActiveTraceBo> appStatList) {
        super(timeWindow, appStatList, BUILDER);
    }

    private static ApplicationStatPoint<Integer> newActiveTracePoint(AggreJoinActiveTraceBo activeTrace) {
        final JoinIntFieldBo totalCountValue = activeTrace.getTotalCountJoinValue();
        long timestamp = activeTrace.getTimestamp();

        return StatPointUtils.toIntStatPoint(timestamp, totalCountValue);
    }

}
