package com.nhn.pinpoint.web.alarm.resource;

import java.util.List;

/**
 * 
 * @author koo.taejin
 */
public class AlarmRuleGroupResourceImpl implements AlarmRuleGroupResource {

	private final int id;
	private final String name;
	private final List<AlarmRuleResource> ruleList;
	
	public AlarmRuleGroupResourceImpl(int id, String name, List<AlarmRuleResource> ruleList) {
		this.id = id;
		this.name = name;
		this.ruleList = ruleList;
	}
	
	@Override
	public int groupId() {
		return id;
	}

	@Override
	public String getGroupName() {
		return name;
	}

	@Override
	public List<AlarmRuleResource> getRuleList() {
		return ruleList;
	}

}
