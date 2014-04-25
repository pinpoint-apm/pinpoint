package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.AlarmEvent;

/**
 * 
 * @author koo.taejin
 */
public interface AlarmFilter {

	boolean execute(AlarmEvent event);
	
}
