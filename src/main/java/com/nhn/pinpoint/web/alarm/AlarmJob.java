package com.nhn.pinpoint.web.alarm;


public interface AlarmJob {

	void execute(AlarmEvent event);

}
