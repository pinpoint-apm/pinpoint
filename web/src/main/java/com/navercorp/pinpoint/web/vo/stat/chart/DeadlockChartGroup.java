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
import com.navercorp.pinpoint.web.vo.chart.TimeSeriesChartBuilder;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class DeadlockChartGroup implements AgentStatChartGroup {

    private static final Integer UNCOLLECTED_DEADLOCK_COUNT = -1;

    private final Map<ChartType, Chart> deadlockCharts;

    public enum DeadlockChartType implements ChartType {
        DEADLOCK_COUNT
    }

    public DeadlockChartGroup(TimeWindow timeWindow, List<SampledDeadlock> sampledDeadlockList) {
        this.deadlockCharts = new HashMap<>();

        List<com.navercorp.pinpoint.web.vo.chart.Point<Long, Integer>> deadlockCountList = new ArrayList<>(sampledDeadlockList.size());
        for (SampledDeadlock sampledDeadlock : sampledDeadlockList) {
            deadlockCountList.add(sampledDeadlock.getDeadlockedThreadCount());
        }
        deadlockCharts.put(DeadlockChartType.DEADLOCK_COUNT, new TimeSeriesChartBuilder<>(timeWindow, UNCOLLECTED_DEADLOCK_COUNT).build(deadlockCountList));
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return this.deadlockCharts;
    }

}
