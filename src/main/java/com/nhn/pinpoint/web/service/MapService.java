package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LoadFactor;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.ResponseHistogramSummary;

/**
 * @author netspider
 */
public interface MapService {
	/**
	 * 메인 화면의 서버 맵 조회.
	 * 
	 * @param sourceApplication
	 * @param range
	 * @return
	 */
	public ApplicationMap selectApplicationMap(Application sourceApplication, Range range);
	
	public ResponseHistogramSummary linkStatistics(Application sourceApplication, Application destinationApplication, Range range);
}
