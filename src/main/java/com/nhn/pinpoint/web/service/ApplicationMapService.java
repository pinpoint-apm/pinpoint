package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.vo.LinkStatistics;

/**
 * @author netspider
 */
public interface ApplicationMapService {
	/**
	 * 메인 화면의 서버 맵 조회.
	 * 
	 * @param applicationName
	 * @param serviceType
	 * @param from
	 * @param to
	 * @return
	 */
	public ApplicationMap selectApplicationMap(String applicationName, short serviceType, long from, long to);
	
	public LinkStatistics linkStatistics(long from, long to, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType);
}
