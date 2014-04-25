package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.AlarmEvent;

/**
 * 
 * @author koo.taejin
 */
public abstract class AlarmCheckFilter implements AlarmFilter {
	
	@Override
	public boolean execute(AlarmEvent event) {
		return check(event);
	}
	
	abstract protected boolean check(AlarmEvent event);
	
}
