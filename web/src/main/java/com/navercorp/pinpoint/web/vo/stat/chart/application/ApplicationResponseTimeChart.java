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
import com.navercorp.pinpoint.web.vo.stat.AggreJoinResponseTimeBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class ApplicationResponseTimeChart implements StatChart {

    private final ApplicationResponseTimeChartGroup applicationResponseTimeChartGroup;

    public ApplicationResponseTimeChart(TimeWindow timeWindow, List<AggreJoinResponseTimeBo> aggreJoinResponseTimeBoList) {
        this.applicationResponseTimeChartGroup = new ApplicationResponseTimeChartGroup(timeWindow, aggreJoinResponseTimeBoList);
    }

    @Override
    public StatChartGroup getCharts() {
        return applicationResponseTimeChartGroup;
    }

    public static class ApplicationResponseTimeChartGroup implements StatChartGroup {

        private static final ResponseTimePoint.UncollectedResponseTimePointCreator UNCOLLECTED_RESPONSE_TIME_POINT = new ResponseTimePoint.UncollectedResponseTimePointCreator();

        private final TimeWindow timeWindow;
        private final Map<ChartType, Chart<? extends Point>> responseTimeChartMap;

        public enum ResponseTimeChartType implements ApplicationChartType {
            RESPONSE_TIME
        }

        public ApplicationResponseTimeChartGroup(TimeWindow timeWindow, List<AggreJoinResponseTimeBo> aggreJoinResponseTimeBoList) {
            this.timeWindow = timeWindow;
            responseTimeChartMap = new HashMap<>();
            List<ResponseTimePoint> responseTimeList = new ArrayList<>(aggreJoinResponseTimeBoList.size());

            for (AggreJoinResponseTimeBo aggreJoinResponseTimeBo : aggreJoinResponseTimeBoList) {
                responseTimeList.add(new ResponseTimePoint(aggreJoinResponseTimeBo.getTimestamp(), aggreJoinResponseTimeBo.getMinAvg(), aggreJoinResponseTimeBo.getMinAvgAgentId(), aggreJoinResponseTimeBo.getMaxAvg(), aggreJoinResponseTimeBo.getMaxAvgAgentId(), aggreJoinResponseTimeBo.getAvg()));
            }
            TimeSeriesChartBuilder<ResponseTimePoint> chartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, UNCOLLECTED_RESPONSE_TIME_POINT);
            responseTimeChartMap.put(ResponseTimeChartType.RESPONSE_TIME, chartBuilder.build(responseTimeList));
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return responseTimeChartMap;
        }
    }
}
