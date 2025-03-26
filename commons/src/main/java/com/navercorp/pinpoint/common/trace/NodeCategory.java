package com.navercorp.pinpoint.common.trace;

public enum NodeCategory {
    UNDEFINED((byte) -1),

    USER((byte) -10),

    UNKNOWN((byte) -20),

    SERVER((byte) 10),

    DATABASE((byte) 20),

    MESSAGE_BROKER((byte) 30),

    CACHE((byte) 40);

//    ROUTER((byte) 50),

    private final byte code;

    NodeCategory(byte code) {
        this.code = code;
    }

    public byte code() {
        return code;
    }
}
