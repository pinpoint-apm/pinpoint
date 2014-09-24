package com.nhn.pinpoint.web.alarm;


public interface AlarmJob {

	boolean execute(AlarmEvent event);

}
