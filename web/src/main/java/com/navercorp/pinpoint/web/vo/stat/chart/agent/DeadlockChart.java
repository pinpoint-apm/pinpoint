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
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class DeadlockChart extends DefaultAgentChart<SampledDeadlock, Integer> {

    public enum DeadlockChartType implements StatChartGroup.AgentChartType {
        DEADLOCK_COUNT
    }

    private static final ChartGroupBuilder<SampledDeadlock, AgentStatPoint<Integer>> BUILDER = newChartBuilder();

    static ChartGroupBuilder<SampledDeadlock, AgentStatPoint<Integer>> newChartBuilder() {
        ChartGroupBuilder<SampledDeadlock, AgentStatPoint<Integer>> builder = new ChartGroupBuilder<>(SampledDeadlock.UNCOLLECTED_POINT_CREATOR);
        builder.addPointFunction(DeadlockChartType.DEADLOCK_COUNT, SampledDeadlock::getDeadlockedThreadCount);
        return builder;
    }

    public DeadlockChart(TimeWindow timeWindow, List<SampledDeadlock> statList) {
        super(timeWindow, statList, BUILDER);
    }

}
