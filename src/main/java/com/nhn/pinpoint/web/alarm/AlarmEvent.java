package com.nhn.pinpoint.web.alarm;

import com.nhn.pinpoint.web.dao.MapStatisticsCallerDao;

/**
 * 
 * @author koo.taejin
 */
public interface AlarmEvent {

	// 머가 더 들어가야 할까
	
	long getEventStartTimeMillis();
	
	MapStatisticsCallerDao getMapStatisticsCallerDao();

}
