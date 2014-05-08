package com.nhn.pinpoint.web.alarm.resource;

public interface MailResource {
	
	String getPinpointUrl();

	String getUrl();
	
	String getServiceId();
	
	String getSenderEmailAddress();
	
	String getOption();

	String getSubject();
	
}
