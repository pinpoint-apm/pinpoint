package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.AlarmEvent;

public class AlarmHeadFilter implements AlarmFilter {

	@Override
	public boolean execute(AlarmEvent event) {
		return true;
	}
	
}
