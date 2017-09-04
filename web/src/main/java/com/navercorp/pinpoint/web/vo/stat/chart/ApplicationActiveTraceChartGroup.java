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
import com.navercorp.pinpoint.web.vo.stat.AggreJoinActiveTraceBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ActiveTracePoint.UncollectedActiveTracePointCreater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class ApplicationActiveTraceChartGroup implements ApplicationStatChartGroup {

    public static final UncollectedActiveTracePointCreater UNCOLLECTED_ACTIVE_TRACT_POINT = new ActiveTracePoint.UncollectedActiveTracePointCreater();
    private final Map<ChartType, Chart> activeTraceChartMap;

    public enum ActiveTraceChartType implements ChartType {
        ACTIVE_TRACE_COUNT
    }

    public ApplicationActiveTraceChartGroup(TimeWindow timeWindow, List<AggreJoinActiveTraceBo> AggreJoinActiveTraceBoList) {
        activeTraceChartMap = new HashMap<>();
        List<Point> activeTraceList = new ArrayList<>(AggreJoinActiveTraceBoList.size());

        for(AggreJoinActiveTraceBo aggreJoinActiveTraceBo : AggreJoinActiveTraceBoList) {
            activeTraceList.add(new ActiveTracePoint(aggreJoinActiveTraceBo.getTimestamp(), aggreJoinActiveTraceBo.getMinTotalCount(), aggreJoinActiveTraceBo.getMinTotalCountAgentId(), aggreJoinActiveTraceBo.getMaxTotalCount(), aggreJoinActiveTraceBo.getMaxTotalCountAgentId(), aggreJoinActiveTraceBo.getTotalCount()));
        }

        activeTraceChartMap.put(ActiveTraceChartType.ACTIVE_TRACE_COUNT, new TimeSeriesChartBuilder(timeWindow, UNCOLLECTED_ACTIVE_TRACT_POINT).build(activeTraceList));
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return this.activeTraceChartMap;
    }
}
