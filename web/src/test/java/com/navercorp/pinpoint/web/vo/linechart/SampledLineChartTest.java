package com.nhn.pinpoint.web.vo.linechart;

import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampledLineChartTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testSampledLongLineChart() throws Exception {
        int sampleRate = 60;
        int totalPoints = 10000;

        SampledLongLineChart lineChart = new SampledLongLineChart(sampleRate);

        for (long i = 0; i < totalPoints; i++) {
            lineChart.addPoint(i, i);
        }

        assertTrue(lineChart.getPoints().size() == totalPoints / sampleRate);

        ObjectMapper mapper = new ObjectMapper();
        String result = mapper.writeValueAsString(lineChart);
        logger.debug(result);
    }

    @Test
    public void testSampledDoubleLineChart() throws Exception {
        int sampleRate = 60;
        int totalPoints = 10000;

        SampledDoubleLineChart lineChart = new SampledDoubleLineChart(sampleRate);

        for (long i = 0; i < totalPoints; i++) {
            lineChart.addPoint(i, (double)i);
        }

        assertTrue(lineChart.getPoints().size() == totalPoints / sampleRate);

        ObjectMapper mapper = new ObjectMapper();
        String result = mapper.writeValueAsString(lineChart);
        logger.debug(result);
    }

}
