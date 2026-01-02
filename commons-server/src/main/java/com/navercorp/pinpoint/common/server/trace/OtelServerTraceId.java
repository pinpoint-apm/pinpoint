package com.navercorp.pinpoint.common.server.trace;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class OtelServerTraceId implements ServerTraceId {
    private final byte[] bytes;

    public OtelServerTraceId(byte[] bytes) {
        this.bytes = Objects.requireNonNull(bytes, "bytes");
        if (bytes.length != 16) {
            throw new IllegalArgumentException("invalid UUID bytes length:"  + bytes.length);
        }

    }

    @Override
    public byte[] getId() {
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        OtelServerTraceId that = (OtelServerTraceId) o;
        return Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        UUID uuid = UUID.nameUUIDFromBytes(bytes);
        return uuid.toString();
    }
}
