/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.linechart;

import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.web.vo.linechart.Chart.ChartBuilder;

public class SampledDataChartBuilderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testSampledLongDataChart() throws Exception {
        // Given
        final long defaultValue = 0L;
        final int sampleRate = 60;
        final int totalPoints = 10000;
        final int expectedNumPoints = getExpectedNumPoints(sampleRate, totalPoints);
        // When
        ChartBuilder<Long, Long> longDataChartBuilder = new SampledDataLongChartBuilder(DownSamplers.getLongDownSampler(defaultValue), sampleRate);
        for (long i = 0; i < totalPoints; ++i) {
            longDataChartBuilder.addDataPoint(new DataPoint<>(i, i));
        }
        Chart lineChart = longDataChartBuilder.buildChart();
        // Then
        assertTrue(lineChart.getPoints().size() == expectedNumPoints);
        ObjectMapper mapper = new ObjectMapper();
        String result = mapper.writeValueAsString(lineChart);
        logger.debug(result);
    }

    @Test
    public void testSampledDoubleLineChart() throws Exception {
        // Given
        final double defaultValue = 0D;
        int sampleRate = 60;
        int totalPoints = 10000;
        final int expectedNumPoints = getExpectedNumPoints(sampleRate, totalPoints);
        // When
        ChartBuilder<Long, Double> doubleLineChartBuilder = new SampledDataDoubleChartBuilder(DownSamplers.getDoubleDownSampler(defaultValue, 1), sampleRate);
        for (long i = 0; i < totalPoints; ++i) {
            doubleLineChartBuilder.addDataPoint(new DataPoint<>(i, (double)i));
        }
        Chart lineChart = doubleLineChartBuilder.buildChart();
        // Then
        assertTrue(lineChart.getPoints().size() == expectedNumPoints);
        ObjectMapper mapper = new ObjectMapper();
        String result = mapper.writeValueAsString(lineChart);
        logger.debug(result);
    }
    
    private int getExpectedNumPoints(int sampleRate, int totalPoints) {
        return totalPoints % sampleRate == 0 ? totalPoints / sampleRate : totalPoints / sampleRate + 1;
    }

}
