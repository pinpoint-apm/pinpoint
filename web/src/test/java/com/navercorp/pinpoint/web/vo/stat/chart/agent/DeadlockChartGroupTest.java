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

import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.DeadlockSampler;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Taejin Koo
 */
public class DeadlockChartGroupTest {

    private static final int RANDOM_LIST_MAX_SIZE = 11; // Random API's upper bound field is exclusive
    private static final int RANDOM_MAX_DEADLOCKED_SIZE = 301; // Random API's upper bound field is exclusive
    private final Random random = new Random();
    private final DeadlockSampler sampler = new DeadlockSampler();

    @Test
    public void basicFunctionTest1() {
        long currentTimeMillis = System.currentTimeMillis();
        TimeWindow timeWindow = new TimeWindow(Range.between(currentTimeMillis - 300000, currentTimeMillis));

        List<SampledDeadlock> sampledDeadlockList = createSampledResponseTimeList(timeWindow);
        StatChart<AgentStatPoint<Integer>> deadlockChartGroup = new DeadlockChart(timeWindow, sampledDeadlockList);

        assertEquals(sampledDeadlockList, deadlockChartGroup.getCharts());
    }

    private List<SampledDeadlock> createSampledResponseTimeList(TimeWindow timeWindow) {
        List<SampledDeadlock> sampledDeadlockList = new ArrayList<>();

        long from = timeWindow.getWindowRange().getFrom();
        long to = timeWindow.getWindowRange().getTo();

        for (long i = from; i < to; i += timeWindow.getWindowSlotSize()) {
            sampledDeadlockList.add(createDeadlock(i));
        }

        return sampledDeadlockList;
    }


    private SampledDeadlock createDeadlock(long timestamp) {
        int listSize = random.nextInt(1, RANDOM_LIST_MAX_SIZE);

        int deadlockedSize = random.nextInt(1, RANDOM_MAX_DEADLOCKED_SIZE);

        List<DeadlockThreadCountBo> deadlockThreadCountBoList = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            DeadlockThreadCountBo deadlockThreadCountBo = new DeadlockThreadCountBo();
            deadlockThreadCountBo.setDeadlockedThreadCount(deadlockedSize + i);
            deadlockThreadCountBoList.add(deadlockThreadCountBo);
        }

        return sampler.sampleDataPoints(0, timestamp, deadlockThreadCountBoList, null);
    }

    private void assertEquals(List<SampledDeadlock> sampledDeadlockList, StatChartGroup<AgentStatPoint<Integer>> deadlockChartGroup) {
        Map<StatChartGroup.ChartType, Chart<AgentStatPoint<Integer>>> charts = deadlockChartGroup.getCharts();

        Chart<AgentStatPoint<Integer>> deadlockCountChart = charts.get(DeadlockChart.DeadlockChartType.DEADLOCK_COUNT);
        List<AgentStatPoint<Integer>> deadlockCountChartPointList = deadlockCountChart.getPoints();

        for (int i = 0; i < sampledDeadlockList.size(); i++) {
            SampledDeadlock sampledDeadlock = sampledDeadlockList.get(i);
            AgentStatPoint<Integer> point = sampledDeadlock.getDeadlockedThreadCount();

            Assertions.assertEquals(deadlockCountChartPointList.get(i), point);
        }
    }

}
