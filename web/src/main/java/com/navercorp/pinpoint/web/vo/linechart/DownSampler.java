package com.nhn.pinpoint.web.vo.linechart;

import java.util.Collection;


/**
 * @author harebox
 * @author hyungil.jeong
 */
public interface DownSampler {

	long sampleLong(Collection<Long> values);
	
	double sampleDouble(Collection<Double> values);
	
}
