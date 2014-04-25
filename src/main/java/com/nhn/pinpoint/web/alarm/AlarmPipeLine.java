package com.nhn.pinpoint.web.alarm;

import com.nhn.pinpoint.web.alarm.filter.AlarmFilter;

/**
 * 
 * @author koo.taejin
 */
public interface AlarmPipeLine {

	void execute(AlarmEvent event);

	boolean addLast(AlarmFilter filter);
	
}
