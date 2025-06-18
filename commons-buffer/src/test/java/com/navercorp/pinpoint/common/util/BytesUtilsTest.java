/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


public class BytesUtilsTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testStringLongLongToBytes() {
        final int strLength = 24;
        byte[] bytes = BytesUtils.stringLongLongToBytes("123", strLength, 12345, 54321);

        assertEquals("123", BytesUtils.toStringAndRightTrim(bytes, 0, strLength));
        assertEquals(12345, ByteArrayUtils.bytesToLong(bytes, strLength));
        assertEquals(54321, ByteArrayUtils.bytesToLong(bytes, strLength + BytesUtils.LONG_BYTE_LENGTH));
    }

    @Test
    public void testStringLongLongToBytes_error() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
            BytesUtils.stringLongLongToBytes("123", 2, 1, 2);
        });
    }

    @Test
    public void testStringLongLongToBytes2() {
        byte[] bytes = BytesUtils.stringLongLongToBytes("123", 10, 1, 2);
        String s = BytesUtils.toStringAndRightTrim(bytes, 0, 10);
        assertEquals("123", s);
        long l = ByteArrayUtils.bytesToLong(bytes, 10);
        assertEquals(1, l);
        long l2 = ByteArrayUtils.bytesToLong(bytes, 10 + BytesUtils.LONG_BYTE_LENGTH);
        assertEquals(2, l2);
    }

    @Test
    public void testInt() {
        int i = Integer.MAX_VALUE - 5;
        checkInt(i);
        checkInt(23464);
    }

    private byte[] intToByteArray(int intValue) {
        return ByteBuffer.allocate(4).putInt(intValue).array();
    }

    private int byteArrayToInt(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).getInt();
    }

    private void checkInt(int i) {
        byte[] bytes = intToByteArray(i);
        int i2 = ByteArrayUtils.bytesToInt(bytes, 0);
        assertEquals(i, i2);
        int i3 = byteArrayToInt(bytes);
        assertEquals(i, i3);
    }

    @Test
    public void testAddStringLong() {
        byte[] testAgents = BytesUtils.add("testAgent", 11L);
        byte[] buf = ByteBuffer.allocate(17).put("testAgent".getBytes()).putLong(11L).array();
        Assertions.assertArrayEquals(testAgents, buf);
    }

    @Test
    public void testAddStringLong_NullError() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            BytesUtils.add((String) null, 11L);
        });
    }

    @Test
    public void testToFixedLengthBytes() {
        byte[] testValue = BytesUtils.toFixedLengthBytes("test", 10);
        assertEquals(10, testValue.length);
        assertEquals(0, testValue[5]);

        byte[] testValue2 = BytesUtils.toFixedLengthBytes(null, 10);
        assertEquals(10, testValue2.length);

    }

    @Test
    public void testToFixedLengthBytes_fail1() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
            BytesUtils.toFixedLengthBytes("test", 2);
        });
    }

    @Test
    public void testToFixedLengthBytes_fail2() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
            BytesUtils.toFixedLengthBytes("test", -1);
        });
    }

    @Test
    public void testConcat() {
        byte[] b1 = new byte[]{1, 2};
        byte[] b2 = new byte[]{3, 4};

        byte[] b3 = BytesUtils.concat(b1, b2);

        Assertions.assertArrayEquals(new byte[]{1, 2, 3, 4}, b3);
    }

    @Test
    public void testZigZag() {
        testEncodingDecodingZigZag(0);
        testEncodingDecodingZigZag(1);
        testEncodingDecodingZigZag(2);
        testEncodingDecodingZigZag(3);
    }

    private void testEncodingDecodingZigZag(int value) {
        int encode = BytesUtils.intToZigZag(value);
        int decode = BytesUtils.zigzagToInt(encode);
        assertEquals(value, decode);
    }


    @Test
    public void testWriteBytes1() {
        byte[] buffer = new byte[10];
        byte[] write = new byte[]{1, 2, 3, 4};

        assertEquals(BytesUtils.writeBytes(buffer, 0, write), write.length);
        Assertions.assertArrayEquals(Arrays.copyOf(buffer, write.length), write);
    }

    @Test
    public void testWriteBytes2() {
        byte[] buffer = new byte[10];
        byte[] write = new byte[]{1, 2, 3, 4};
        int startOffset = 1;
        assertEquals(BytesUtils.writeBytes(buffer, startOffset, write), write.length + startOffset);
        Assertions.assertArrayEquals(Arrays.copyOfRange(buffer, startOffset, write.length + startOffset), write);
    }

    @Test
    public void testAppropriateWriteBytes() {
        byte[] dst = new byte[10];
        byte[] src = new byte[5];
        src[0] = 1;
        src[1] = 2;
        src[2] = 3;
        src[3] = 4;
        src[4] = 5;
        // proper return?
        assertEquals(3, BytesUtils.writeBytes(dst, 1, src, 2, 2));
        // successful write?
        assertEquals(3, dst[1]);
        assertEquals(4, dst[2]);
    }

    @Test
    public void testOverflowDestinationWriteBytes() {
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            byte[] dst = new byte[5];
            byte[] src = new byte[10];
            for (int i = 0; i < 10; i++) {
                src[i] = (byte) (i + 1);
            }

            // overflow!
            BytesUtils.writeBytes(dst, 0, src);
            // if it does not catch any errors, it means memory leak!
        });
    }

    @Test
    public void testAppropriateBytesToLong() {
        byte[] such_long = new byte[12];
        for (int i = 0; i < 12; i++) {
            such_long[i] = (byte) ((i << 4) + i);
        }
        assertEquals(0x33445566778899AAl, ByteArrayUtils.bytesToLong(such_long, 3));
    }

    @Test
    public void testOverflowBytesToLong() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
            byte[] such_long = new byte[12];
            for (int i = 0; i < 12; i++) {
                such_long[i] = (byte) ((i << 4) + i);
            }
            // overflow!
            ByteArrayUtils.bytesToLong(such_long, 9);
            // if it does not catch any errors, it means memory leak!
        });
    }

    @Test
    public void testWriteLong_npe() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            ByteArrayUtils.writeLong(1234, null, 0);
        });
    }

    @Test()
    public void testWriteLong_fail() {

        byte[] such_long = new byte[13];
        try {
            ByteArrayUtils.writeLong(1234, such_long, -1);
            fail("negative offset did not catched");
        } catch (Exception ignored) {
        }

        try {
            ByteArrayUtils.writeLong(2222, such_long, 9);
            fail("index out of range exception did not catched");
        } catch (Exception ignored) {
        }

        ByteArrayUtils.writeLong(-1l, such_long, 2);
        for (int i = 2; i < 10; i++) {
            assertEquals((byte) 0xFF, such_long[i]);
        }
    }

    @Test
    public void testByteRightTrim1() {
        byte[] bytes = writeBytes(3, "123", 0, 3);
        assertEquals("123", BytesUtils.toStringAndRightTrim(bytes, 0, 3));

        byte[] testByte2 = writeBytes(10, "123", 0, 3);
        assertEquals("123", BytesUtils.toStringAndRightTrim(testByte2, 0, 10));

        byte[] testByte3 = writeBytes(10, "", 0, 3);
        assertEquals("", BytesUtils.toStringAndRightTrim(testByte3, 0, 10));
    }

    private byte[] writeBytes(int bufferSize, String s, int offset, int length) {
        byte[] buffer = new byte[bufferSize];
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(bytes, offset, buffer, offset, Math.min(bytes.length, length));
        return buffer;
    }

    @Test
    public void testByteRightTrim2() {
        // no space
        byte[] testByte1 = "0123456789 abc".getBytes(StandardCharsets.UTF_8);
        assertEquals("23456789", BytesUtils.toStringAndRightTrim(testByte1, 2, 9));
        // right spaced
        byte[] testByte2 = "0123456789 abcabc!       ".getBytes(StandardCharsets.UTF_8);
        assertEquals(" abcabc!", BytesUtils.toStringAndRightTrim(testByte2, 10, 10));
    }

    @Test
    public void testRightTrimIndex1() {
        String testStr = "0123  ";
        byte[] testBytes = testStr.getBytes(StandardCharsets.UTF_8);
        assertEquals(testStr.trim().length(), BytesUtils.rightTrimIndex(testBytes, 0, testBytes.length));
    }

    @Test
    public void testRightTrimIndex2() {
        String testStr = "0123  ";
        byte[] testBytes = testStr.getBytes(StandardCharsets.UTF_8);
        assertEquals(testStr.trim().length(), BytesUtils.rightTrimIndex(testBytes, 1, testBytes.length - 1));
    }

    @Test
    public void testRightTrimIndex3() {
        byte[] testBytes = new byte[0];
        assertEquals(0, BytesUtils.rightTrimIndex(testBytes, 0, testBytes.length));
    }

    /**
     * bound 1->0
     * bound 2->128
     * bound 3->16384
     * bound 4->2097152
     * bound 5->268435456
     */
    @Test
    public void testBoundaryValueVar32() {
        Assertions.assertEquals(1, BytesUtils.computeVar32Size(0));

        Assertions.assertEquals(1, BytesUtils.computeVar32Size(127));

        Assertions.assertEquals(2, BytesUtils.computeVar32Size(128));
        Assertions.assertEquals(2, BytesUtils.computeVar32Size(16383));

        Assertions.assertEquals(3, BytesUtils.computeVar32Size(16384));
        Assertions.assertEquals(4, BytesUtils.computeVar32Size(2097152));
        Assertions.assertEquals(5, BytesUtils.computeVar32Size(268435456));

        Assertions.assertEquals(5, BytesUtils.computeVar32Size(-1));
    }

    /**
     * bound 1->0
     * bound 2->128
     * bound 3->16384
     * bound 4->2097152
     * bound 5->268435456
     * bound 6->34359738368
     * bound 7->?
     * bound 8->?
     * bound 9->?
     * bound 10->?
     */
    @Test
    public void testComputeVar64Size() {
        Assertions.assertEquals(1, BytesUtils.computeVar64Size(0));
        Assertions.assertEquals(1, BytesUtils.computeVar64Size(127));

        Assertions.assertEquals(2, BytesUtils.computeVar64Size(128));
        Assertions.assertEquals(2, BytesUtils.computeVar64Size(16383));

        Assertions.assertEquals(3, BytesUtils.computeVar64Size(16384));
        Assertions.assertEquals(4, BytesUtils.computeVar64Size(2097152));
        Assertions.assertEquals(5, BytesUtils.computeVar64Size(268435456));
        Assertions.assertEquals(6, BytesUtils.computeVar64Size(34359738368L));

        Assertions.assertEquals(10, BytesUtils.computeVar64Size(-1));
    }

    @Test
    public void testVar32() {

        assertVar32(127);
        assertVar32(128);

        assertVar32(16383);
        assertVar32(16384);

        assertVar32(2097151);
        assertVar32(2097152);

        assertVar32(268435455);
        assertVar32(268435456);
        assertVar32(Integer.MAX_VALUE - 1);
        assertVar32(Integer.MAX_VALUE);
        assertVar32(Integer.MIN_VALUE);
        assertVar32(Integer.MIN_VALUE + 1);

        assertVar32(-127);
        assertVar32(-128);
        assertVar32(-16383);
        assertVar32(-16384);
        assertVar32(-268435455);
        assertVar32(-268435456);
    }

    @Test
    public void testVar32_indexCheck() {
        final byte[] bytes = new byte[1];
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> BytesUtils.writeVar32(256, bytes, 0));
    }

    private void assertVar32(int value) {
        final int computeBufferSize = BytesUtils.computeVar32Size(value);
        final byte[] bytes = new byte[computeBufferSize];
        BytesUtils.writeVar32(value, bytes, 0);

        final Buffer buffer = new FixedBuffer(bytes);
        final int varInt = buffer.readVInt();
        assertEquals(value, varInt, "check value");
        assertEquals(buffer.getOffset(), computeBufferSize, "check buffer size");

        final int varInt_ByteUtils1 = BytesUtils.bytesToVar32(buffer.getBuffer(), 0);
        assertEquals(value, varInt_ByteUtils1, "check value");

        final byte[] max_buffer = new byte[BytesUtils.VLONG_MAX_SIZE];
        BytesUtils.writeVar32(value, max_buffer, 0);
        final int varInt_ByteUtils2 = BytesUtils.bytesToVar32(max_buffer, 0);
        assertEquals(value, varInt_ByteUtils2, "check value");


    }

    @Test
    public void testVar64() {

        assertVar64(127);
        assertVar64(128);

        assertVar64(16383);
        assertVar64(16384);

        assertVar64(2097151);
        assertVar64(2097152);

        assertVar64(268435455);
        assertVar64(268435456);

        assertVar64(34359738367L);
        assertVar64(34359738368L);


        assertVar64(Long.MAX_VALUE - 1);
        assertVar64(Long.MAX_VALUE);
        assertVar64(Long.MIN_VALUE);
        assertVar64(Long.MIN_VALUE + 1);

        assertVar64(-127);
        assertVar64(-128);
        assertVar64(-2097151);
        assertVar64(-2097152);
        assertVar64(-34359738367L);
        assertVar64(-34359738368L);
    }

    @Test
    public void testVar64_indexCheck() {
        final byte[] bytes = new byte[1];
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> BytesUtils.writeVar64(256, bytes, 0));
    }

    @Test
    public void testIntToSVar32() {
        assertIntToSVar32(Integer.MAX_VALUE);
        assertIntToSVar32(Integer.MIN_VALUE);
        assertIntToSVar32(0);
        assertIntToSVar32(1);
        assertIntToSVar32(-1);
    }

    private void assertIntToSVar32(int value) {
        assertEquals(BytesUtils.bytesToSVar32(BytesUtils.intToSVar32(value), 0), value);
    }

    @Test
    public void testIntToVar32() {
        assertIntToVar32(Integer.MAX_VALUE);
        assertIntToVar32(Integer.MIN_VALUE);
        assertIntToVar32(0);
        assertIntToVar32(1);
        assertIntToVar32(-1);
    }

    private void assertIntToVar32(int value) {
        assertEquals(BytesUtils.bytesToVar32(BytesUtils.intToVar32(value), 0), value);
    }

    private void assertVar64(long value) {
        final int computeBufferSize = BytesUtils.computeVar64Size(value);
        final byte[] bytes = new byte[computeBufferSize];
        BytesUtils.writeVar64(value, bytes, 0);

        final Buffer buffer = new FixedBuffer(bytes);
        final long varLong = buffer.readVLong();
        assertEquals(value, varLong, "check value");
        assertEquals(buffer.getOffset(), computeBufferSize, "check buffer size");

        final long varLong_ByteUtils1 = BytesUtils.bytesToVar64(buffer.getBuffer(), 0);
        assertEquals(value, varLong_ByteUtils1, "check value");

        final byte[] max_buffer = new byte[BytesUtils.VLONG_MAX_SIZE];
        BytesUtils.writeVar64(value, max_buffer, 0);
        final long varLong_ByteUtils2 = BytesUtils.bytesToVar64(max_buffer, 0);
        assertEquals(value, varLong_ByteUtils2, "check value");
    }

    @Test
    public void testCheckBound() {
        final byte[] buffer = new byte[10];
        BytesUtils.checkBounds(buffer, 0, buffer.length);
        BytesUtils.checkBounds(buffer, 2, buffer.length - 2);
        BytesUtils.checkBounds(buffer, 0, buffer.length - 1);
    }

    @Test
    public void testCheckBound_fail() {
        final byte[] buffer = new byte[10];

        try {
            BytesUtils.checkBounds(buffer, buffer.length, buffer.length);
            fail("bound check fail");
        } catch (Exception ignored) {
        }

        try {
            BytesUtils.checkBounds(buffer, 2, buffer.length);
            fail("bound check fail");
        } catch (Exception ignored) {
        }

        try {
            BytesUtils.checkBounds(buffer, -1, buffer.length);
            fail("bound check fail");
        } catch (Exception ignored) {
        }

        try {
            BytesUtils.bytesToSVar32(new byte[10], 10);
            fail("bound check fail");
        } catch (Exception ignored) {
        }

    }

    @Test
    public void testShortToUnsignedShort() {
        assertEquals(0, BytesUtils.shortToUnsignedShort((short) 0));
        assertEquals(32767, BytesUtils.shortToUnsignedShort(Short.MAX_VALUE));
        final short maxOver = (short) (Short.MAX_VALUE + 1);
        assertEquals(32768, BytesUtils.shortToUnsignedShort(maxOver));
        assertEquals(65535, BytesUtils.shortToUnsignedShort((short) -1));
    }

    @Test
    public void checkBounds() {
        BytesUtils.checkFromIndexSize(0, 0, 0);

        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> BytesUtils.checkFromIndexSize(-1, 0, 0));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> BytesUtils.checkFromIndexSize(0, -1, 0));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> BytesUtils.checkFromIndexSize(0, 0, -1));
    }

    @Test
    public void checkBounds_exceptionMessage() {
        ArrayIndexOutOfBoundsException error = Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            BytesUtils.checkFromIndexSize(1, 4, 0);
        });
        assertEquals("Out of range 5", error.getMessage());
    }
}
