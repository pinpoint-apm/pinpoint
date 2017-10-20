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
import com.navercorp.pinpoint.web.vo.stat.AggreJoinResponseTimeBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ResponseTimePoint.UncollectedResponseTimePointCreater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class ApplicationResponseTimeChartGroup implements ApplicationStatChartGroup {

    public static final UncollectedResponseTimePointCreater UNCOLLECTED_RESPONSETIME_POINT = new UncollectedResponseTimePointCreater();

    private final Map<ChartType, Chart> responseTimeChartMap;

    public enum ResponseTimeChartType implements ChartType {
        RESPONSE_TIME
    }

    public ApplicationResponseTimeChartGroup(TimeWindow timeWindow, List<AggreJoinResponseTimeBo> aggreJoinResponseTimeBoList) {
        responseTimeChartMap = new HashMap<>();
        List<Point> responseTimeList = new ArrayList<>(aggreJoinResponseTimeBoList.size());

        for (AggreJoinResponseTimeBo aggreJoinResponseTimeBo : aggreJoinResponseTimeBoList) {
            responseTimeList.add(new ResponseTimePoint(aggreJoinResponseTimeBo.getTimestamp(), aggreJoinResponseTimeBo.getMinAvg(), aggreJoinResponseTimeBo.getMinAvgAgentId(), aggreJoinResponseTimeBo.getMaxAvg(), aggreJoinResponseTimeBo.getMaxAvgAgentId(), aggreJoinResponseTimeBo.getAvg()));
        }

        responseTimeChartMap.put(ResponseTimeChartType.RESPONSE_TIME, new TimeSeriesChartBuilder(timeWindow, UNCOLLECTED_RESPONSETIME_POINT).build(responseTimeList));
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return responseTimeChartMap;
    }
}
