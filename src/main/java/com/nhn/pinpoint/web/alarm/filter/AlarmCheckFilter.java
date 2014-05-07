package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.AlarmEvent;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;

/**
 * 
 * @author koo.taejin
 */
public abstract class AlarmCheckFilter implements AlarmFilter {
	
	private AlarmRuleResource rule;
	
	public void initialize(AlarmRuleResource rule) {
		this.rule = rule;
	}
	
	@Override
	public boolean execute(AlarmEvent event) {
		return check(event);
	}
	
	abstract protected boolean check(AlarmEvent event);

	protected AlarmRuleResource getRule() {
		return rule;
	}

}
