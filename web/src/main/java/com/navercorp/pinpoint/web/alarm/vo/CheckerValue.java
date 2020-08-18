package com.navercorp.pinpoint.web.alarm.vo;

public abstract class CheckerValue {
    
    private final String unit;
    
    public CheckerValue(String unit) {
        this.unit = unit;
    }
    
    public String getUnit() {
        return unit;
    }
}
