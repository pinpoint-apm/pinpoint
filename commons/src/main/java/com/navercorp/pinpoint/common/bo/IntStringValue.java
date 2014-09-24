package com.nhn.pinpoint.common.bo;

/**
 * @author emeroad
 */
public class IntStringValue {
    private final int intValue;
    private final String stringValue;

    public IntStringValue(int intValue, String stringValue) {
        this.intValue = intValue;
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public int getIntValue() {
        return intValue;
    }
}
