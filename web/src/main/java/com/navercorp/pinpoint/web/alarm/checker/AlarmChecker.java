package com.nhn.pinpoint.web.alarm.checker;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

/**
 * 
 * @author koo.taejin
 */
public abstract class AlarmChecker {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final DataCollector dataCollector;
    protected final Rule rule;
    protected boolean detected = false;
    protected final String unit;
    
    protected AlarmChecker(Rule rule, String unit, DataCollector dataCollector) {
        this.rule = rule;
        this.unit = unit;
        this.dataCollector = dataCollector;
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
    
    protected boolean decideResult(long value) {
        if (value >= rule.getThreshold()) {
            return true;
        } else {
            return false;
        }
    }

    public void check() {
        dataCollector.collect();
        
        if (decideResult(getDetectedValue())) {
            detected = true;
        } else {
            detected = false;
        }
        
        logger.info("{} result is {} for application ({}). value is {}. (threshold : {}).", this.getClass().getSimpleName(), detected, rule.getApplicationId(), getDetectedValue(), rule.getThreshold());
    }
    
    public List<String> getSmsMessage() {
        List<String> messages = new LinkedList<String>();
        messages.add(String.format("[PINPOINT Alarm - %s] %s is %s%s (Threshold : %s%s)", rule.getApplicationId(), rule.getCheckerName(), getDetectedValue(), unit, rule.getThreshold(), unit));
        return messages;
    };
    
    public String getEmailMessage() {
        return String.format("%s value is %s%s during the past 5 mins.(Threshold : %s%s)<br>", rule.getCheckerName(), getDetectedValue(), unit, rule.getThreshold(), unit);
    };
    
    protected abstract long getDetectedValue();

    
}
