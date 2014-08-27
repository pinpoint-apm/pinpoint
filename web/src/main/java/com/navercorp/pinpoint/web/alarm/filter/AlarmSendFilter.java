package com.nhn.pinpoint.web.alarm.filter;

import java.util.List;

import com.nhn.pinpoint.web.alarm.AlarmEvent;

/**
 * 
 * @author koo.taejin
 */
public abstract class AlarmSendFilter implements AlarmFilter {
	
	abstract public boolean send(List<AlarmCheckFilter> checkFilterList, AlarmEvent event);
	
}
