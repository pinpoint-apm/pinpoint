package com.navercorp.pinpoint.common.server.util;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Objects;

public final class ByteStringUtils {
    private ByteStringUtils() {
    }

    public static boolean isEmpty(ByteString buf) {
        return buf == null || buf.isEmpty();
    }

    public static long parseLong(final ByteString buf) {
        return parseLong(buf, 0);
    }

    public static long parseLong(final ByteString buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, BytesUtils.LONG_BYTE_LENGTH);

        return (((long) buf.byteAt(offset) & 0xff) << 56)
                | (((long) buf.byteAt(offset + 1) & 0xff) << 48)
                | (((long) buf.byteAt(offset + 2) & 0xff) << 40)
                | (((long) buf.byteAt(offset + 3) & 0xff) << 32)
                | (((long) buf.byteAt(offset + 4) & 0xff) << 24)
                | (((long) buf.byteAt(offset + 5) & 0xff) << 16)
                | (((long) buf.byteAt(offset + 6) & 0xff) << 8)
                | (((long) buf.byteAt(offset + 7) & 0xff));
    }

    public static int parseInt(final ByteString buf) {
        return parseInt(buf, 0);
    }

    public static int parseInt(final ByteString buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, BytesUtils.INT_BYTE_LENGTH);

        return ((buf.byteAt(offset) & 0xff) << 24)
                | ((buf.byteAt(offset + 1) & 0xff) << 16)
                | ((buf.byteAt(offset + 2) & 0xff) << 8)
                | ((buf.byteAt(offset + 3) & 0xff));
    }

    public static void checkBounds(ByteString bytes, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, bytes.size());
    }
}
