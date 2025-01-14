package com.navercorp.pinpoint.common.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UuidUtils {

    public static final int UUID_BYTE_LENGTH = 16;

    public static byte[] toBytes(UUID uuid) {
        final ByteBuffer buffer = ByteBuffer.allocate(UUID_BYTE_LENGTH);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    public static UUID toUUID(byte[] bytes) {
        return toUUID(bytes, 0);
    }

    public static UUID toUUID(byte[] bytes, int offset) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes is null");
        }
        if (bytes.length - offset < UUID_BYTE_LENGTH) {
            throw new IllegalArgumentException("too short bytes.length " + bytes.length);
        }

        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, offset, UUID_BYTE_LENGTH);
        final long high = byteBuffer.getLong();
        final long low = byteBuffer.getLong();
        return new UUID(high, low);
    }
}