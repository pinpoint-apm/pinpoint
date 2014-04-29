package com.nhn.pinpoint.web.alarm.resource;

import java.util.List;

/**
 * 
 * @author koo.taejin
 */
public interface AlarmRuleGroupResource {

	int groupId();
	
	String getGroupName();
	
	List<AlarmRuleResource> getRuleList();
	
}
