package com.navercorp.pinpoint.web.alarm.vo;

public abstract class CheckerDetectedValue {
    
    private final String unit;
    
    protected CheckerDetectedValue(String unit) {
        this.unit = unit;
    }
    
    public String getUnit() {
        return unit;
    }
}
