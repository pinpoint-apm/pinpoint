package com.nhn.pinpoint.web.alarm.resource;

/**
 * 
 * @author koo.taejin
 */
public class AlarmResourceImpl {

	private final AlarmRuleGroupResource ruleGroupResource;
	private final AlarmContactResource contactResource;

	public AlarmResourceImpl(AlarmRuleGroupResource ruleGroupResource, AlarmContactResource contactResource) {
		this.ruleGroupResource = ruleGroupResource;
		this.contactResource = contactResource;
	}

	public AlarmRuleGroupResource getRuleGroupResource() {
		return ruleGroupResource;
	}

	public AlarmContactResource getContactResource() {
		return contactResource;
	}

}
