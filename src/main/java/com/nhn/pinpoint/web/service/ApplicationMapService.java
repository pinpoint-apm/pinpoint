package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;

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
}
