package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;
import com.nhn.pinpoint.web.alarm.vo.Rule;

/**
 * 
 * @author koo.taejin
 */
public abstract class AlarmCheckFilter implements AlarmFilter {

    protected final Rule rule;
    protected boolean detected = false;
    protected final String unit;
    
    protected AlarmCheckFilter(Rule rule, String unit) {
        this.rule = rule;
        this.unit = unit;
    }
    
    public boolean isDetected() {
        return detected;
        
    }
    
    public Rule getRule() {
        return rule;
    }
    
    public boolean isSMSSend() {
        return rule.isSmsSend();
    }
    
    public boolean isEmailSend() {
        return rule.isEmailSend();
    }
    
    public String getEmpGroup() {
        return rule.getEmpGroup();
    }
    
    public String getUnit() {
        return unit;
    }

    abstract public void check();
    
    abstract public String getDetectedValue();
    
    

    
}
