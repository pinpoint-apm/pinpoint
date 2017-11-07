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
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
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
public class DeadlockChart implements StatChart {

    private final DeadlockChartGroup deadlockChartGroup;

    public DeadlockChart(TimeWindow timeWindow, List<SampledDeadlock> sampledDeadlocks) {
        this.deadlockChartGroup = new DeadlockChartGroup(timeWindow, sampledDeadlocks);
    }

    @Override
    public StatChartGroup getCharts() {
        return deadlockChartGroup;
    }

    public static class DeadlockChartGroup implements StatChartGroup {

        private final TimeWindow timeWindow;

        private final Map<ChartType, Chart<? extends Point>> deadlockCharts;

        public enum DeadlockChartType implements AgentChartType {
            DEADLOCK_COUNT
        }

        public DeadlockChartGroup(TimeWindow timeWindow, List<SampledDeadlock> sampledDeadlockList) {
            this.timeWindow = timeWindow;
            this.deadlockCharts = newChart(sampledDeadlockList);
        }

        public Map<ChartType, Chart<? extends Point>> newChart(List<SampledDeadlock> deadlockList) {
            List<AgentStatPoint<Integer>> chartSource = filter(deadlockList, SampledDeadlock::getDeadlockedThreadCount);
            TimeSeriesChartBuilder<AgentStatPoint<Integer>> chartBuilder = new TimeSeriesChartBuilder<>(this.timeWindow, SampledDeadlock.UNCOLLECTED_POINT_CREATER);
            Chart<AgentStatPoint<Integer>> chart = chartBuilder.build(chartSource);

            return Collections.singletonMap(DeadlockChartType.DEADLOCK_COUNT, chart);
        }

        public List<AgentStatPoint<Integer>> filter(List<SampledDeadlock> deadlockList, Function<SampledDeadlock, AgentStatPoint<Integer>> filter) {
            if (CollectionUtils.isEmpty(deadlockList)) {
                return Collections.emptyList();
            }
            List<AgentStatPoint<Integer>> result = new ArrayList<>(deadlockList.size());
            for (SampledDeadlock sampledDeadlock : deadlockList) {
                AgentStatPoint<Integer> apply = filter.apply(sampledDeadlock);
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
            return this.deadlockCharts;
        }
    }

}
