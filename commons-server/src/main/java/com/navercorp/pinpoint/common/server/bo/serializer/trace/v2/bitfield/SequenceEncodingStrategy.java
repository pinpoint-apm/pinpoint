package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum SequenceEncodingStrategy {
    // 1 bit
    PREV_ADD1(0),
    PREV_DELTA(1);

    private final int code;

    SequenceEncodingStrategy(int code) {
        this.code = code;
    }


}
