package com.nhn.pinpoint.web.vo.linechart;

import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.vo.linechart.Chart.ChartBuilder;

public class SampledDataChartBuilderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testSampledLongDataChart() throws Exception {
        // Given
        final int sampleRate = 60;
        final int totalPoints = 10000;
        final int expectedNumPoints = getExpectedNumPoints(sampleRate, totalPoints);
        // When
        ChartBuilder<Long, Long> longDataChartBuilder = new SampledDataLongChartBuilder(sampleRate);
        for (long i = 0; i < totalPoints; ++i) {
            longDataChartBuilder.addDataPoint(new DataPoint<Long, Long>(i, i));
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
        int sampleRate = 60;
        int totalPoints = 10000;
        final int expectedNumPoints = getExpectedNumPoints(sampleRate, totalPoints);
        // When
        ChartBuilder<Long, Double> doubleLineChartBuilder = new SampledDataDoubleChartBuilder(sampleRate);
        for (long i = 0; i < totalPoints; ++i) {
            doubleLineChartBuilder.addDataPoint(new DataPoint<Long, Double>(i, (double)i));
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
