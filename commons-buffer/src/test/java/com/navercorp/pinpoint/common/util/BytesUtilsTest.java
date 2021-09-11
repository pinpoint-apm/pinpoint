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

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BytesUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testStringLongLongToBytes() {
        final int strLength = 24;
        byte[] bytes = BytesUtils.stringLongLongToBytes("123", strLength, 12345, 54321);

        Assert.assertEquals("123", BytesUtils.toStringAndRightTrim(bytes, 0, strLength));
        Assert.assertEquals(12345, BytesUtils.bytesToLong(bytes, strLength));
        Assert.assertEquals(54321, BytesUtils.bytesToLong(bytes, strLength + BytesUtils.LONG_BYTE_LENGTH));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testStringLongLongToBytes_error() {
        BytesUtils.stringLongLongToBytes("123", 2, 1, 2);
    }

    @Test
    public void testStringLongLongToBytes2() {
        byte[] bytes = BytesUtils.stringLongLongToBytes("123", 10, 1, 2);
        String s = BytesUtils.toStringAndRightTrim(bytes, 0, 10);
        Assert.assertEquals("123", s);
        long l = BytesUtils.bytesToLong(bytes, 10);
        Assert.assertEquals(l, 1);
        long l2 = BytesUtils.bytesToLong(bytes, 10 + BytesUtils.LONG_BYTE_LENGTH);
        Assert.assertEquals(l2, 2);
    }

    @Test
    public void testRightTrim() {
        String trim = BytesUtils.rightTrim("test  ");
        Assert.assertEquals("test", trim);

        String trim1 = BytesUtils.rightTrim("test");
        Assert.assertEquals("test", trim1);

        String trim2 = BytesUtils.rightTrim("  test");
        Assert.assertEquals("  test", trim2);

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
        int i2 = BytesUtils.bytesToInt(bytes, 0);
        Assert.assertEquals(i, i2);
        int i3 = byteArrayToInt(bytes);
        Assert.assertEquals(i, i3);
    }

    @Test
    public void testAddStringLong() {
        byte[] testAgents = BytesUtils.add("testAgent", 11L);
        byte[] buf = ByteBuffer.allocate(17).put("testAgent".getBytes()).putLong(11L).array();
        Assert.assertArrayEquals(testAgents, buf);
    }

    @Test(expected = NullPointerException.class)
    public void testAddStringLong_NullError() {
        BytesUtils.add((String) null, 11L);
    }

    @Test
    public void testToFixedLengthBytes() {
        byte[] testValue = BytesUtils.toFixedLengthBytes("test", 10);
        Assert.assertEquals(testValue.length, 10);
        Assert.assertEquals(testValue[5], 0);

        byte[] testValue2 = BytesUtils.toFixedLengthBytes(null, 10);
        Assert.assertEquals(testValue2.length, 10);

    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testToFixedLengthBytes_fail1() {
        BytesUtils.toFixedLengthBytes("test", 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testToFixedLengthBytes_fail2() {
        BytesUtils.toFixedLengthBytes("test", -1);
    }

    @Test
    public void testConcat() {
        byte[] b1 = new byte[] { 1, 2 };
        byte[] b2 = new byte[] { 3, 4 };

        byte[] b3 = BytesUtils.concat(b1, b2);

        Assert.assertArrayEquals(new byte[] { 1, 2, 3, 4 }, b3);
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
        Assert.assertEquals(value, decode);
    }



    @Test
    public void testWriteBytes1() {
        byte[] buffer = new byte[10];
        byte[] write = new byte[] { 1, 2, 3, 4 };

        Assert.assertEquals(BytesUtils.writeBytes(buffer, 0, write), write.length);
        Assert.assertArrayEquals(Arrays.copyOf(buffer, write.length), write);
    }

    @Test
    public void testWriteBytes2() {
        byte[] buffer = new byte[10];
        byte[] write = new byte[] { 1, 2, 3, 4 };
        int startOffset = 1;
        Assert.assertEquals(BytesUtils.writeBytes(buffer, startOffset, write), write.length + startOffset);
        Assert.assertArrayEquals(Arrays.copyOfRange(buffer, startOffset, write.length + startOffset), write);
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
        Assert.assertEquals(3, BytesUtils.writeBytes(dst, 1, src, 2, 2));
        // successful write?
        Assert.assertEquals(3, dst[1]);
        Assert.assertEquals(4, dst[2]);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testOverflowDestinationWriteBytes() {
        byte[] dst = new byte[5];
        byte[] src = new byte[10];
        for (int i = 0; i < 10; i++) {
            src[i] = (byte) (i + 1);
        }

        // overflow!
        BytesUtils.writeBytes(dst, 0, src);
        // if it does not catch any errors, it means memory leak!
    }

    @Test
    public void testAppropriateBytesToLong() {
        byte[] such_long = new byte[12];
        int i;
        for (i = 0; i < 12; i++) {
            such_long[i] = (byte) ((i << 4) + i);
        }
        Assert.assertEquals(0x33445566778899AAl, BytesUtils.bytesToLong(such_long, 3));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOverflowBytesToLong() {
        byte[] such_long = new byte[12];
        int i;
        for (i = 0; i < 12; i++) {
            such_long[i] = (byte) ((i << 4) + i);
        }
        // overflow!
        BytesUtils.bytesToLong(such_long, 9);
        // if it does not catch any errors, it means memory leak!
    }

    @Test(expected = NullPointerException.class)
    public void testWriteLong_npe() {
        BytesUtils.writeLong(1234, null, 0);
    }

    @Test()
    public void testWriteLong_fail() {

        byte[] such_long = new byte[13];
        try {
            BytesUtils.writeLong(1234, such_long, -1);
            fail("negative offset did not catched");
        } catch (Exception ignore) {
        }

        try {
            BytesUtils.writeLong(2222, such_long, 9);
            fail("index out of range exception did not catched");
        } catch (Exception ignore) {
        }

        BytesUtils.writeLong(-1l, such_long, 2);
        for (int i = 2; i < 10; i++) {
            Assert.assertEquals((byte) 0xFF, such_long[i]);
        }
    }

    @Test
    public void testRightTrim2() {
        // no space
        String testStr = "0123456789 abc";
        Assert.assertEquals("0123456789 abc", BytesUtils.rightTrim(testStr));
        // right spaced
        testStr = "0123456789 abcabc!       ";
        Assert.assertEquals("0123456789 abcabc!", BytesUtils.rightTrim(testStr));
    }

    @Test
    public void testByteRightTrim1() {
        byte[] bytes = writeBytes(3, "123", 0, 3);
        Assert.assertEquals("123", BytesUtils.toStringAndRightTrim(bytes, 0, 3));

        byte[] testByte2 = writeBytes(10, "123", 0, 3);
        Assert.assertEquals("123", BytesUtils.toStringAndRightTrim(testByte2, 0, 10));

        byte[] testByte3 = writeBytes(10, "", 0, 3);
        Assert.assertEquals("", BytesUtils.toStringAndRightTrim(testByte3, 0, 10));
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
        Assert.assertEquals("23456789", BytesUtils.toStringAndRightTrim(testByte1, 2, 9));
        // right spaced
        byte[] testByte2 = "0123456789 abcabc!       ".getBytes(StandardCharsets.UTF_8);
        Assert.assertEquals(" abcabc!", BytesUtils.toStringAndRightTrim(testByte2, 10, 10));
    }

    @Test
    public void testRightTrimIndex1() {
        String testStr = "0123  ";
        byte[] testBytes = testStr.getBytes(StandardCharsets.UTF_8);
        Assert.assertEquals(testStr.trim().length(), BytesUtils.rightTrimIndex(testBytes, 0, testBytes.length));
    }

    @Test
    public void testRightTrimIndex2() {
        String testStr = "0123  ";
        byte[] testBytes = testStr.getBytes(StandardCharsets.UTF_8);
        Assert.assertEquals(testStr.trim().length(), BytesUtils.rightTrimIndex(testBytes, 1, testBytes.length - 1));
    }

    @Test
    public void testRightTrimIndex3() {
        byte[] testBytes = new byte[0];
        Assert.assertEquals(0, BytesUtils.rightTrimIndex(testBytes, 0, testBytes.length));
    }

    @Test
    public void toStringAndRightTrim_empty() {
        assertEquals(BytesUtils.rightTrim(""), "");
        assertEquals(BytesUtils.rightTrim(" "), "");
        assertEquals(BytesUtils.rightTrim("  "), "");
        assertEquals(BytesUtils.rightTrim("     "), "");
    }

    @Test
    public void toStringAndRightTrim() {
        assertEquals(BytesUtils.rightTrim("1"), "1");
        assertEquals(BytesUtils.rightTrim("2 "), "2");
        assertEquals(BytesUtils.rightTrim("3  "), "3");
        assertEquals(BytesUtils.rightTrim("4     "), "4");

        assertEquals(BytesUtils.rightTrim("5 1 "), "5 1");
    }

    /**
     * bound 1->0
     * bound 2->128
     * bound 3->16384
     * bound 4->2097152
     * bound 5->268435456
     */
//    @Test
    public void testBoundaryValueVar32() {
        int boundSize = 0;
        for (int i =0; i< Integer.MAX_VALUE; i++) {
            final int size = BytesUtils.computeVar32Size(i);
            if (size> boundSize) {
                boundSize = size;
                logger.debug("bound {}->{}", boundSize, i);
            }

        }
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
//    @Test
    public void testBoundaryValueVar64() {
        int boundSize = 0;
        for (long i =0; i< Long.MAX_VALUE; i++) {
            final int size = BytesUtils.computeVar64Size(i);
            if (size> boundSize) {
                boundSize = size;
                logger.debug("bound {}->{}", boundSize, i);
            }
        }
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
        assertVar32(Integer.MAX_VALUE-1);
        assertVar32(Integer.MAX_VALUE);
        assertVar32(Integer.MIN_VALUE);
        assertVar32(Integer.MIN_VALUE+1);

        assertVar32(-127);
        assertVar32(-128);
        assertVar32(-16383);
        assertVar32(-16384);
        assertVar32(-268435455);
        assertVar32(-268435456);
    }

    private void assertVar32(int value) {
        final int computeBufferSize = BytesUtils.computeVar32Size(value);
        final byte[] bytes = new byte[computeBufferSize];
        BytesUtils.writeVar32(value, bytes, 0);

        final Buffer buffer = new FixedBuffer(bytes);
        final int varInt = buffer.readVInt();
        Assert.assertEquals("check value", value, varInt);
        assertEquals("check buffer size", buffer.getOffset(), computeBufferSize);

        final int varInt_ByteUtils1 = BytesUtils.bytesToVar32(buffer.getBuffer(), 0);
        Assert.assertEquals("check value", value, varInt_ByteUtils1);

        final byte[] max_buffer = new byte[BytesUtils.VLONG_MAX_SIZE];
        BytesUtils.writeVar32(value, max_buffer, 0);
        final int varInt_ByteUtils2 = BytesUtils.bytesToVar32(max_buffer, 0);
        Assert.assertEquals("check value", value, varInt_ByteUtils2);


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


        assertVar64(Long.MAX_VALUE-1);
        assertVar64(Long.MAX_VALUE);
        assertVar64(Long.MIN_VALUE);
        assertVar64(Long.MIN_VALUE+1);

        assertVar64(-127);
        assertVar64(-128);
        assertVar64(-2097151);
        assertVar64(-2097152);
        assertVar64(-34359738367L);
        assertVar64(-34359738368L);
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
        Assert.assertEquals(BytesUtils.bytesToSVar32(BytesUtils.intToSVar32(value), 0), value);
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
        Assert.assertEquals(BytesUtils.bytesToVar32(BytesUtils.intToVar32(value), 0), value);
    }

    private void assertVar64(long value) {
        final int computeBufferSize = BytesUtils.computeVar64Size(value);
        final byte[] bytes = new byte[computeBufferSize];
        BytesUtils.writeVar64(value, bytes, 0);

        final Buffer buffer = new FixedBuffer(bytes);
        final long varLong = buffer.readVLong();
        Assert.assertEquals("check value", value, varLong);
        assertEquals("check buffer size", buffer.getOffset(), computeBufferSize);

        final long varLong_ByteUtils1 = BytesUtils.bytesToVar64(buffer.getBuffer(), 0);
        Assert.assertEquals("check value", value, varLong_ByteUtils1);

        final byte[] max_buffer = new byte[BytesUtils.VLONG_MAX_SIZE];
        BytesUtils.writeVar64(value, max_buffer, 0);
        final long varLong_ByteUtils2 = BytesUtils.bytesToVar64(max_buffer, 0);
        Assert.assertEquals("check value", value, varLong_ByteUtils2);
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
            Assert.fail("bound check fail");
        } catch (Exception ignore) {
        }

        try {
            BytesUtils.checkBounds(buffer, 2, buffer.length);
            Assert.fail("bound check fail");
        } catch (Exception ignore) {
        }

        try {
            BytesUtils.checkBounds(buffer, -1, buffer.length);
            Assert.fail("bound check fail");
        } catch (Exception ignore) {
        }

        try {
            BytesUtils.bytesToSVar32(new byte[10], 10);
            Assert.fail("bound check fail");
        } catch (Exception ignored) {
        }

    }

    @Test
    public void testShortToUnsignedShort() {
        Assert.assertEquals(BytesUtils.shortToUnsignedShort((short)0), 0);
        Assert.assertEquals(BytesUtils.shortToUnsignedShort(Short.MAX_VALUE), 32767);
        final short maxOver = (short) (Short.MAX_VALUE + 1);
        Assert.assertEquals(BytesUtils.shortToUnsignedShort(maxOver), 32768);
        Assert.assertEquals(BytesUtils.shortToUnsignedShort((short)-1), 65535);
    }
}
