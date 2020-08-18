package com.navercorp.pinpoint.web.alarm.vo;

public class AlarmCheckerValue<T> extends CheckerValue {
    
    private T detectedValue;
    
    public AlarmCheckerValue(String unit, T detectedValue) {
        super(unit);
        this.detectedValue = detectedValue;
    }
    
    public T getDetectedValue() {
        return detectedValue;
    }
}
