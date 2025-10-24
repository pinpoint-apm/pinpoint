package com.navercorp.pinpoint.common.trace;

public enum ErrorCategory {
    UNKNOWN(1 << 0),
    EXCEPTION(1 << 1),
    HTTP_STATUS(1 << 2),
    SQL(1 << 3);

    private final int bitMask;

    ErrorCategory(int bitMask) {
        this.bitMask = bitMask;
    }

    public int getBitMask() {
        return bitMask;
    }
}
