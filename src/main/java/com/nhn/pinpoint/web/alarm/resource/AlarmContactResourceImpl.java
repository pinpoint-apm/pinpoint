package com.nhn.pinpoint.web.alarm.resource;

import java.util.List;

/**
 * 
 * @author koo.taejin
 */

public class AlarmContactResourceImpl implements AlarmContactResource {

	private final int id;
	private final String name;
	
	private final List<String> phoneNumberList;
	private final List<String> emailList;
	
	public AlarmContactResourceImpl(int id, String name, List<String> phoneNumberList, List<String> emailList) {
		this.id = id;
		this.name = name;
		this.phoneNumberList = phoneNumberList;
		this.emailList = emailList;
	}
	
	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getPhoneNumberList() {
		return phoneNumberList;
	}

	@Override
	public List<String> getEmailList() {
		return emailList;
	}

}
