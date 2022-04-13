/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTotalThreadCountBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

public class ApplicationTotalThreadCountChart extends DefaultApplicationChart<AggreJoinTotalThreadCountBo, Long> {

    private static final Point.UncollectedPointCreator<ApplicationStatPoint<Long>> UNCOLLECTED_POINT
            = new LongApplicationStatPoint.UncollectedCreator(AggreJoinTotalThreadCountBo.UNCOLLECTED_VALUE);

    private static final ChartGroupBuilder<AggreJoinTotalThreadCountBo, ApplicationStatPoint<Long>> BUILDER = newChartBuilder();

    public enum TotalThreadCountChartType implements StatChartGroup.ApplicationChartType {
        TOTAL_THREAD_COUNT
    }

    static ChartGroupBuilder<AggreJoinTotalThreadCountBo, ApplicationStatPoint<Long>> newChartBuilder() {
        ChartGroupBuilder<AggreJoinTotalThreadCountBo, ApplicationStatPoint<Long>> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(TotalThreadCountChartType.TOTAL_THREAD_COUNT, ApplicationTotalThreadCountChart::newTotalThreadCount);
        return builder;
    }

    public ApplicationTotalThreadCountChart(TimeWindow timeWindow, List<AggreJoinTotalThreadCountBo> appStatList) {
        super(timeWindow, appStatList, BUILDER);
    }

    private static ApplicationStatPoint<Long> newTotalThreadCount(AggreJoinTotalThreadCountBo totalThreadCountBo) {
        final JoinLongFieldBo totalThreadCountJoinValue = totalThreadCountBo.getTotalThreadCountJoinValue();
        long timestamp = totalThreadCountBo.getTimestamp();
        return StatPointUtils.toLongStatPoint(timestamp, totalThreadCountJoinValue);
    }

}
