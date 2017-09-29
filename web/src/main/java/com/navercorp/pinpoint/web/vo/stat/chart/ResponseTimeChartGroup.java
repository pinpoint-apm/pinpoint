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
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ResponseTimeChartGroup implements AgentStatChartGroup {

    private static final Long UNCOLLECTED_RESPONSE_TIME = -1L;

    private final Map<ChartType, Chart> responseTimeCharts;

    public enum ResponseTimeChartType implements ChartType {
        AVG
    }

    public ResponseTimeChartGroup(TimeWindow timeWindow, List<SampledResponseTime> sampledResponseTimes) {
        this.responseTimeCharts = new HashMap<>();
        List<Point<Long, Long>> avg = new ArrayList<>(sampledResponseTimes.size());
        for (SampledResponseTime sampledResponseTime : sampledResponseTimes) {
            avg.add(sampledResponseTime.getAvg());
        }
        responseTimeCharts.put(ResponseTimeChartType.AVG, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_RESPONSE_TIME).build(avg));
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return this.responseTimeCharts;
    }

}
