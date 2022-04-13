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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinResponseTimeBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class ApplicationResponseTimeChart extends DefaultApplicationChart<AggreJoinResponseTimeBo, Double> {

    private static final Point.UncollectedPointCreator<ApplicationStatPoint<Double>> UNCOLLECTED_POINT
            = new DoubleApplicationStatPoint.UncollectedCreator(JoinResponseTimeBo.UNCOLLECTED_VALUE);


    private static final ChartGroupBuilder<AggreJoinResponseTimeBo, ApplicationStatPoint<Double>> BUILDER = newChartBuilder();

    public enum ResponseTimeChartType implements StatChartGroup.ApplicationChartType {
        RESPONSE_TIME;
    }

    static ChartGroupBuilder<AggreJoinResponseTimeBo, ApplicationStatPoint<Double>> newChartBuilder() {
        ChartGroupBuilder<AggreJoinResponseTimeBo, ApplicationStatPoint<Double>> builder = new ChartGroupBuilder<>(UNCOLLECTED_POINT);
        builder.addPointFunction(ResponseTimeChartType.RESPONSE_TIME, ApplicationResponseTimeChart::newResponseTime);
        return builder;
    }

    public ApplicationResponseTimeChart(TimeWindow timeWindow, List<AggreJoinResponseTimeBo> appStatList) {
        super(timeWindow, appStatList, BUILDER);
    }

    private static ApplicationStatPoint<Double> newResponseTime(AggreJoinResponseTimeBo time) {
        final JoinLongFieldBo responseTimeJoinValue = time.getResponseTimeJoinValue();
        long timestamp = time.getTimestamp();
        return StatPointUtils.longToDoubleStatPoint(timestamp, responseTimeJoinValue);
    }

}
