package com.navercorp.pinpoint.uid.vo;

public record ApplicationUidAttribute(String applicationName, int serviceTypeCode) {
    public static final byte SEPARATOR = (byte) '@';

    @Override
    public String toString() {
        return "ApplicationUidAttribute{" +
                applicationName + (char) SEPARATOR + serviceTypeCode +
                '}';
    }
}
