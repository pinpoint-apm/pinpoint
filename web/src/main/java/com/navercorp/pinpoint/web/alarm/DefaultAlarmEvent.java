package com.nhn.pinpoint.web.alarm;

import com.nhn.pinpoint.web.dao.MapStatisticsCallerDao;

public class DefaultAlarmEvent implements AlarmEvent {

	private final long startEventTimeMillis;
	private MapStatisticsCallerDao mapStatisticsCallerDao;

	public DefaultAlarmEvent(long startEventTimeMillis) {
		this.startEventTimeMillis = startEventTimeMillis;
	}

	@Override
	public long getEventStartTimeMillis() {
		return startEventTimeMillis;
	}

	public MapStatisticsCallerDao getMapStatisticsCallerDao() {
		return mapStatisticsCallerDao;
	}

	public void setMapStatisticsCallerDao(MapStatisticsCallerDao mapStatisticsCallerDao) {
		this.mapStatisticsCallerDao = mapStatisticsCallerDao;
	}

}
