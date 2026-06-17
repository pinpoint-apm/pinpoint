package com.navercorp.pinpoint.common.server.util;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ByteStringUtils {
    private static final byte[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };


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

    /**
     * Encodes the full {@code bytes} to lower-case Base16 (hex). See
     * {@link #encodeBase16(ByteString, int)} for the length-capped form.
     */
    public static String encodeBase16(ByteString bytes) {
        return encodeBase16(bytes, Integer.MAX_VALUE);
    }

    /**
     * Encodes {@code bytes} to lower-case Base16 (hex), capped at {@code maxLength} characters. Each
     * byte yields 2 hex chars, so when the full encoding would exceed the limit only the first
     * {@code ceil(maxLength / 2)} bytes are encoded and the result is cut to exactly {@code maxLength}
     * (hex is pure ASCII, so a char cut is a byte cut — no multi-byte boundary concern). Returns an
     * empty string for empty input or non-positive {@code maxLength}; throws {@link ArithmeticException}
     * when {@code bytes.size() * 2} overflows int (> ~1GB).
     */
    public static String encodeBase16(ByteString bytes, int maxLength) {
        if (bytes.isEmpty() || maxLength <= 0) {
            return "";
        }
        // encodedLength is a long so size*2 can't overflow; min caps it back within int maxLength.
        // Non-empty + positive maxLength guarantees hexLength >= 1 here.
        final int hexLength = (int) Math.min(Base16Utils.encodedLength(bytes.size()), maxLength);
        // Encode directly from the ByteString into a byte[] of exactly hexLength (no source-bytes
        // copy, no over-encoding past the cap). Each byte yields 2 lower-case hex chars; an odd
        // hexLength keeps the high nibble of the last byte (matching a char-level cut of full hex).
        final byte[] out = new byte[hexLength];
        int oi = 0;
        for (int i = 0; oi < hexLength; i++) {
            final int b = bytes.byteAt(i) & 0xFF;
            out[oi++] = HEX[b >>> 4];
            if (oi < hexLength) {
                out[oi++] = HEX[b & 0x0F];
            }
        }
        // hex is pure ASCII; ISO-8859-1 maps each byte straight to its char (fast, no validation)
        return new String(out, StandardCharsets.ISO_8859_1);
    }
}
