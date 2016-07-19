package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum SimpleServiceTypeEncodingStrategy {

    // 1 bit
    PREV_EQUALS(0),
    RAW(1);

    private final int code;

    SimpleServiceTypeEncodingStrategy(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
