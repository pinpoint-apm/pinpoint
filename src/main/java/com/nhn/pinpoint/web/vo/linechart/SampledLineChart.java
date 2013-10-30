package com.nhn.pinpoint.web.vo.linechart;

import java.security.InvalidParameterException;


public final class SampledLineChart extends LineChart {

	int sampleRate;
	int sampleIndex;
	Long[] sampleBuffer;
	
	public SampledLineChart(int sampleRate) {
		this.sampleRate = sampleRate;
		this.sampleBuffer = new Long[sampleRate];
		this.sampleIndex = 0;
	}
	
	@Override
	public void addPoint(Long[] point) {
		if (point == null || point.length != 2) {
			throw new InvalidParameterException("point array should be Number[2]");
		}
		
		sampleBuffer[sampleIndex++] = point[1];
		
		// FIXME 선택 가능하게. 모두 다 하는 경우에는 그냥 메소드 한번으로 끝낼 수도 있지만 일단...
		if (sampleIndex == sampleRate) {
			// point[x, minY, maxY, avgY]
			Long[] samplePoint = new Long[4];
			samplePoint[0] = point[0];
			samplePoint[1] = DownSamplers.MIN.sampleLong(sampleBuffer);
			samplePoint[2] = DownSamplers.MAX.sampleLong(sampleBuffer);
			samplePoint[3] = DownSamplers.AVG.sampleLong(sampleBuffer);
			
			getPoints().add(samplePoint);
			sampleIndex = 0;
		}
	}

}
