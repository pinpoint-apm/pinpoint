package com.nhn.pinpoint.web.vo.linechart;


/**
 * @author harebox
 */
public interface DownSampler {

	long sampleLong(Long[] longs);
	
	double sampleDouble(Double[] doubles);
	
}
