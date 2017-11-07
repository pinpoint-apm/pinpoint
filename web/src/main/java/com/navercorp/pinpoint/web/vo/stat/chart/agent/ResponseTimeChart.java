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

package com.navercorp.pinpoint.web.vo.stat.chart.agent;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Taejin Koo
 */
public class ResponseTimeChart implements StatChart {

    private final ResponseTimeChartGroup responseTimeChartGroup;

    public ResponseTimeChart(TimeWindow timeWindow, List<SampledResponseTime> sampledResponseTimes) {
        this.responseTimeChartGroup = new ResponseTimeChartGroup(timeWindow, sampledResponseTimes);
    }

    @Override
    public StatChartGroup getCharts() {
        return responseTimeChartGroup;
    }

    public static class ResponseTimeChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private final Map<ChartType, Chart<? extends Point>> responseTimeCharts;

        public enum ResponseTimeChartType implements AgentChartType {
            AVG
        }

        public ResponseTimeChartGroup(TimeWindow timeWindow, List<SampledResponseTime> sampledResponseTimes) {
            this.timeWindow = timeWindow;
            this.responseTimeCharts = newChart(sampledResponseTimes);
        }

        private Map<ChartType, Chart<? extends Point>> newChart(List<SampledResponseTime> sampledResponseTimes) {
            List<AgentStatPoint<Long>> chartSource = filter(sampledResponseTimes, SampledResponseTime::getAvg);
            TimeSeriesChartBuilder<AgentStatPoint<Long>> chartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledResponseTime.UNCOLLECTED_POINT_CREATER);
            Chart<AgentStatPoint<Long>> chart = chartBuilder.build(chartSource);

            return Collections.singletonMap(ResponseTimeChartType.AVG, chart);
        }

        private List<AgentStatPoint<Long>> filter(List<SampledResponseTime> sampledResponseTimes, Function<SampledResponseTime, AgentStatPoint<Long>> filter) {
            if (CollectionUtils.isEmpty(sampledResponseTimes)) {
                return Collections.emptyList();
            }

            List<AgentStatPoint<Long>> result = new ArrayList<>(sampledResponseTimes.size());
            for (SampledResponseTime sampledResponseTime : sampledResponseTimes) {
                AgentStatPoint<Long> apply = filter.apply(sampledResponseTime);
                result.add(apply);
            }
            return result;
        }

        @Override
        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        @Override
        public Map<ChartType, Chart<? extends Point>> getCharts() {
            return this.responseTimeCharts;
        }
    }

}
