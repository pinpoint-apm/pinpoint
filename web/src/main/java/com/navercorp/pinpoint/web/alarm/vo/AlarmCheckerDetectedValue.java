package com.navercorp.pinpoint.web.alarm.vo;

public class AlarmCheckerDetectedValue<T> extends CheckerDetectedValue {
    
    private T detectedValue;
    
    public AlarmCheckerDetectedValue(String unit, T detectedValue) {
        super(unit);
        this.detectedValue = detectedValue;
    }
    
    public T getDetectedValue() {
        return detectedValue;
    }
}
