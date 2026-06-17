package com.navercorp.pinpoint.common.util;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ByteStringUtilsTest {


    @Test
    void testParseLongValidInput() {
        byte[] bytes = writeLong(Long.MAX_VALUE);
        ByteString byteString = ByteString.copyFrom(bytes);

        long result = ByteStringUtils.parseLong(byteString, 0);
        Assertions.assertEquals(Long.MAX_VALUE, result);
    }

    private byte[] writeLong(long value) {
        byte[] bytes = new byte[BytesUtils.LONG_BYTE_LENGTH];
        BytesUtils.writeLong(value, bytes, 0);
        return bytes;
    }

    @Test
    void testParseLong_invalidOffset() {
        byte[] bytes = writeLong(Long.MAX_VALUE);
        ByteString byteString = ByteString.copyFrom(bytes);

        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ByteStringUtils.parseLong(byteString, 1));
    }

    @Test
    void testParseIntValidInput() {
        byte[] bytes = writeInt(Integer.MAX_VALUE);
        ByteString byteString = ByteString.copyFrom(bytes);

        long result = ByteStringUtils.parseInt(byteString, 0);
        Assertions.assertEquals(Integer.MAX_VALUE, result);
    }

    private byte[] writeInt(int value) {
        byte[] bytes = new byte[BytesUtils.INT_BYTE_LENGTH];
        BytesUtils.writeInt(value, bytes, 0);
        return bytes;
    }

    @Test
    void testParseInt_invalidOffset() {
        byte[] bytes = writeInt(Integer.MAX_VALUE);
        ByteString byteString = ByteString.copyFrom(bytes);

        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ByteStringUtils.parseInt(byteString, 1));
    }

    @Test
    void encodeBase16_empty_returnsEmptyString() {
        Assertions.assertEquals("", ByteStringUtils.encodeBase16(ByteString.EMPTY, 8));
    }

    @Test
    void encodeBase16_withinLimit_fullHex() {
        ByteString buf = ByteString.copyFrom(new byte[]{0x0a, (byte) 0xbc});

        Assertions.assertEquals("0abc", ByteStringUtils.encodeBase16(buf, 64));
    }

    @Test
    void encodeBase16_overLimit_cutToMaxLength() {
        ByteString buf = ByteString.copyFrom(new byte[16]); // 16 bytes -> 32 hex chars

        String result = ByteStringUtils.encodeBase16(buf, 10);

        Assertions.assertEquals(10, result.length());
        Assertions.assertEquals("0000000000", result);
    }

    @Test
    void encodeBase16_oddLimit_cutToExactLength() {
        ByteString buf = ByteString.copyFrom(new byte[16]);

        // odd maxLength: ceil(5/2)=3 bytes -> 6 hex chars, then cut to 5
        Assertions.assertEquals("00000", ByteStringUtils.encodeBase16(buf, 5));
    }

}