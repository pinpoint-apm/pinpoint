package com.nhn.pinpoint.web.alarm.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

/**
 * 
 * @author koo.taejin
 */
public abstract class AlarmCheckFilter implements AlarmFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final DataCollector dataCollector;
    protected final Rule rule;
    protected boolean detected = false;
    protected final String unit;
    
    protected AlarmCheckFilter(Rule rule, String unit, DataCollector dataCollector) {
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
        logger.debug("{} check.", this.getClass().getSimpleName());
        dataCollector.collect();
        
        if (decideResult(getDetectedValue())) {
            detected = true;
        } else {
            detected = false;
        }
    }
    
    abstract public long getDetectedValue();

    
}
