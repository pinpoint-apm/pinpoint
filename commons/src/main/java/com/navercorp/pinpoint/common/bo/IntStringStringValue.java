package com.nhn.pinpoint.common.bo;

/**
 * @author emeroad
 */
public class IntStringStringValue {
    private final int intValue;
    private final String stringValue1;
    private final String stringValue2;

    public IntStringStringValue(int intValue, String stringValue1, String stringValue2) {
        this.intValue = intValue;
        this.stringValue1 = stringValue1;
        this.stringValue2 = stringValue2;
    }

    public int getIntValue() {
        return intValue;
    }

    public String getStringValue1() {
        return stringValue1;
    }

    public String getStringValue2() {
        return stringValue2;
    }
}
