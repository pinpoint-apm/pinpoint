package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.applicationmap.ApplicationStatistics;

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
	 * @param hideIndirectAccess
	 * @return
	 */
	public ApplicationMap selectApplicationMap(String applicationName, short serviceType, long from, long to, boolean hideIndirectAccess);
	
	/**
	 * 서버 통계정보 조회
	 * 
	 * @param applicationName
	 * @param serviceType
	 * @param from
	 * @param to
	 * @return
	 */
	public ApplicationStatistics selectApplicationStatistics(String applicationName, short serviceType, long from, long to);
}
