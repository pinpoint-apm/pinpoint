package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LinkStatistics;

/**
 * @author netspider
 */
public interface ApplicationMapService {
	/**
	 * 메인 화면의 서버 맵 조회.
	 * 
	 * @param sourceApplication
	 * @param from
	 * @param to
	 * @return
	 */
	public ApplicationMap selectApplicationMap(Application sourceApplication, long from, long to);
	
	public LinkStatistics linkStatistics(Application sourceApplication, Application destinationApplication, long from, long to);
}
