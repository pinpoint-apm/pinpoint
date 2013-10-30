package com.nhn.pinpoint.web.vo.linechart;

import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class SampledLineChartTest {

	@Test
	public void tdd() throws Exception {
		int sampleRate = 60;
		int totalPoints = 10000;
		
		LineChart lineChart = new SampledLineChart(sampleRate);
		
		for (long i = 0; i < totalPoints; i++) {
			lineChart.addPoint(new Long[]{i, i});
		}
		
		assertTrue(lineChart.getPoints().size() == totalPoints / sampleRate);
		
		ObjectMapper mapper = new ObjectMapper();
		String result = mapper.writeValueAsString(lineChart);
		System.out.println(result);
	}

}
