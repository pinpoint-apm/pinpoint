package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.AlarmEvent;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;

/**
 * 
 * @author koo.taejin
 */
public abstract class AlarmCheckFilter implements AlarmFilter {
	
	private AlarmRuleResource rule;
	
	abstract public boolean check(AlarmEvent event);

	public void initialize(AlarmRuleResource rule) {
		this.rule = rule;
	}

	public AlarmRuleResource getRule() {
		return rule;
	}

}
