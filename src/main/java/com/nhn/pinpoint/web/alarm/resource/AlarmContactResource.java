package com.nhn.pinpoint.web.alarm.resource;

import java.util.List;

/**
 * 
 * @author koo.taejin
 */
public interface AlarmContactResource {

	int getId();
	String getName();
	
	List<String> getPhoneNumberList();
	List<String> getEmailList();

}
