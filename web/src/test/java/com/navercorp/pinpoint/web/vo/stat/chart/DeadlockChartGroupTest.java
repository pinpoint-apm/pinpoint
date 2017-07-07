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

import com.navercorp.pinpoint.common.server.bo.stat.DeadlockBo;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.DeadlockSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class DeadlockChartGroupTest {

    private static final int RANDOM_LIST_MAX_SIZE = 11; // Random API's upper bound field is exclusive
    private static final int RANDOM_MAX_DEADLOCKED_SIZE = 301; // Random API's upper bound field is exclusive

    private final DeadlockSampler sampler = new DeadlockSampler();

    @Test
    public void basicFunctionTest1() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        TimeWindow timeWindow = new TimeWindow(new Range(currentTimeMillis - 300000, currentTimeMillis));

        List<SampledDeadlock> sampledDeadlockList = createSampledResponseTimeList(timeWindow);
        DeadlockChartGroup deadlockChartGroup = new DeadlockChartGroup(timeWindow, sampledDeadlockList);

        assertEquals(sampledDeadlockList, deadlockChartGroup);
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
        int listSize = RandomUtils.nextInt(1, RANDOM_LIST_MAX_SIZE);

        int deadlockedSize = RandomUtils.nextInt(1, RANDOM_MAX_DEADLOCKED_SIZE);

        List<DeadlockBo> deadlockBoList = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            DeadlockBo deadlockBo = new DeadlockBo();
            deadlockBo.setDeadlockedThreadCount(deadlockedSize + i);
            deadlockBoList.add(deadlockBo);
        }

        return sampler.sampleDataPoints(0, timestamp, deadlockBoList, null);
    }

    private void assertEquals(List<SampledDeadlock> sampledDeadlockList, DeadlockChartGroup deadlockChartGroup) {
        Map<AgentStatChartGroup.ChartType, Chart> charts = deadlockChartGroup.getCharts();

        Chart deadlockCountChart = charts.get(DeadlockChartGroup.DeadlockChartType.DEADLOCK_COUNT);
        List<Point> deadlockCountChartPointList = deadlockCountChart.getPoints();

        for (int i = 0; i < sampledDeadlockList.size(); i++) {
            SampledDeadlock sampledDeadlock = sampledDeadlockList.get(i);
            Point<Long, Integer> point = sampledDeadlock.getDeadlockedThreadCount();

            Assert.assertEquals(deadlockCountChartPointList.get(i), point);
        }
    }

}
