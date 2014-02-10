package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.Range;

/**
 * @author netspider
 */
public interface ApplicationMapService {
	/**
	 * 메인 화면의 서버 맵 조회.
	 * 
	 * @param sourceApplication
	 * @param range
	 * @return
	 */
	public ApplicationMap selectApplicationMap(Application sourceApplication, Range range);
	
	public LinkStatistics linkStatistics(Application sourceApplication, Application destinationApplication, Range range);
}
