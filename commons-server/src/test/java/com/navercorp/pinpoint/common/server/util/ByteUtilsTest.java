package com.navercorp.pinpoint.common.server.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ByteUtilsTest {

    @Test
    void toUnsignedByte() {
        assertThrows(IllegalArgumentException.class, () -> ByteUtils.toUnsignedByte(-1));
        assertThrows(IllegalArgumentException.class, () -> ByteUtils.toUnsignedByte(256));

        Assertions.assertEquals(0, ByteUtils.toUnsignedByte(0));
        Assertions.assertEquals(10, ByteUtils.toUnsignedByte(10));
        Assertions.assertEquals(Byte.MAX_VALUE, ByteUtils.toUnsignedByte(Byte.MAX_VALUE));

        Assertions.assertEquals(126, ByteUtils.toUnsignedByte(126));
        Assertions.assertEquals(-2, ByteUtils.toUnsignedByte(254));
        Assertions.assertEquals(-1, ByteUtils.toUnsignedByte(255));
    }

    @Test
    void toUnsignedByte1() {
        Assertions.assertEquals(0, assertUnsignedByte(0));
        Assertions.assertEquals(1, assertUnsignedByte(1));
        Assertions.assertEquals(255, assertUnsignedByte(255));
    }

    private int assertUnsignedByte(int value) {
        byte unsignedByte = ByteUtils.toUnsignedByte(value);
        return Byte.toUnsignedInt(unsignedByte);
    }
}