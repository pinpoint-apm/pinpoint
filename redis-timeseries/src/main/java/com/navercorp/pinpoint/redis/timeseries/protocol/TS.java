package com.navercorp.pinpoint.redis.timeseries.protocol;

import io.lettuce.core.protocol.ProtocolKeyword;

import java.nio.charset.StandardCharsets;

public enum TS implements ProtocolKeyword {

    CREATE, ADD,
    DEL,
    RANGE, REVRANGE, GET;

    public static final String PREFIX = "TS";

    public final byte[] bytes;

    TS() {
        this.bytes = (PREFIX + '.' + name()).getBytes(StandardCharsets.US_ASCII);
    }


    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
