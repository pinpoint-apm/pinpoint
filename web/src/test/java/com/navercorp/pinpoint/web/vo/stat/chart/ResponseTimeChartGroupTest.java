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

import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ResponseTimeSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Taejin Koo
 */
public class ResponseTimeChartGroupTest {

    private static final int MIN_VALUE_OF_MAX_CONNECTION_SIZE = 20;
    private static final int RANDOM_LIST_MAX_SIZE = 10;
    private static final int RANDOM_AVG_MAX_SIZE = 300000;

    private final ResponseTimeSampler sampler = new ResponseTimeSampler();

    @Test
    public void basicFunctionTest1() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        TimeWindow timeWindow = new TimeWindow(new Range(currentTimeMillis - 300000, currentTimeMillis));

        List<SampledResponseTime> sampledResponseTimeList = createSampledResponseTimeList(timeWindow);

        ResponseTimeChartGroup responseTimeChartGroup = new ResponseTimeChartGroup(timeWindow, sampledResponseTimeList);

        assertEquals(sampledResponseTimeList, responseTimeChartGroup);
    }

    private List<SampledResponseTime> createSampledResponseTimeList(TimeWindow timeWindow) {
        List<SampledResponseTime> sampledResponseTimeList = new ArrayList<>();

        int maxConnectionSize = ThreadLocalRandom.current().nextInt(MIN_VALUE_OF_MAX_CONNECTION_SIZE) + MIN_VALUE_OF_MAX_CONNECTION_SIZE;

        long from = timeWindow.getWindowRange().getFrom();
        long to = timeWindow.getWindowRange().getTo();

        for (long i = from; i < to; i += timeWindow.getWindowSlotSize()) {
            sampledResponseTimeList.add(createSampledResponseTime(i, maxConnectionSize));
        }

        return sampledResponseTimeList;
    }

    private SampledResponseTime createSampledResponseTime(long timestamp, int maxConnectionSize) {
        int listSize = RandomUtils.nextInt(1, RANDOM_LIST_MAX_SIZE);

        List<ResponseTimeBo> responseTimeBoList = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            ResponseTimeBo responseTimeBo = new ResponseTimeBo();
            responseTimeBo.setAvg(ThreadLocalRandom.current().nextLong(RANDOM_AVG_MAX_SIZE));
            responseTimeBoList.add(responseTimeBo);
        }

        return sampler.sampleDataPoints(0, timestamp, responseTimeBoList, null);
    }

    private void assertEquals(List<SampledResponseTime> sampledResponseTimeList, ResponseTimeChartGroup responseTimeChartGroup) {
        Map<AgentStatChartGroup.ChartType, Chart> charts = responseTimeChartGroup.getCharts();

        Chart avgChart = charts.get(ResponseTimeChartGroup.ResponseTimeChartType.AVG);
        List<Point> avgChartPointList = avgChart.getPoints();

        for (int i = 0; i < sampledResponseTimeList.size(); i++) {
            SampledResponseTime sampledResponseTime = sampledResponseTimeList.get(i);
            Point<Long, Long> point = sampledResponseTime.getAvg();

            Assert.assertEquals(avgChartPointList.get(i), point);
        }
    }

}
