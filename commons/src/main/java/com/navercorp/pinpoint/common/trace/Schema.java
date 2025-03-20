package com.navercorp.pinpoint.common.trace;

public enum Schema {
    FAST(1),
    NORMAL(2);

    private final int type;

    Schema(int type) {
        this.type = type;
    }

    public int type() {
        return type;
    }

    public static Schema getSchema(int type) {
        switch (type) {
            case 1:
                return FAST;
            case 2:
                return NORMAL;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }
}
