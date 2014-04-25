package com.nhn.pinpoint.web.alarm;

import com.nhn.pinpoint.web.alarm.filter.AlarmFilter;

/**
 * 
 * @author koo.taejin
 */
public interface AlarmFilterContext {

//	void execute(AlarmEvent event);

	boolean canCheck();
	boolean check(AlarmEvent event);

	boolean canSend();
	void send(AlarmEvent event);
	
	AlarmFilterContext getPrev();
	AlarmFilterContext getNext();

	AlarmFilter getFilter();
	
}
