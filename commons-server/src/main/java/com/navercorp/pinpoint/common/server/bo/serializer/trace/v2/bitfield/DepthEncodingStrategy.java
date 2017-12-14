package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum DepthEncodingStrategy {
    // 1bit
    PREV_EQUALS(0),
    RAW(1);

    private final int code;

    DepthEncodingStrategy(int code) {
        this.code = code;
    }
}
