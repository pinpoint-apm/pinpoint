package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.AlarmEvent;

/**
 * 
 * @author koo.taejin
 */
public abstract class AlarmSendFilter implements AlarmFilter {

	abstract protected boolean send(AlarmEvent event);
	
	@Override
	public boolean execute(AlarmEvent event) {
		return send(event);
	}
	
}
