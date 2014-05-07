package com.nhn.pinpoint.web.alarm.filter;

public abstract class AlarmCheckCountFilter extends AlarmCheckFilter  {

	protected boolean check(long count) {
		int threshold = getRule().getThresholdRule();
		
		if (count > threshold) {
			return true;
		} else {
			return false;
		}
	}

}
