/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.buffer;

import com.navercorp.pinpoint.common.Charsets;
import com.navercorp.pinpoint.common.util.BytesUtils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Random;

/**
 * @author emeroad
 */
public class AutomaticBufferTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Random random = new Random();

    @Test
    public void testPutPrefixedBytes() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.putInt(1);
        byte[] buf = buffer.getBuffer();
        Assert.assertEquals(buf.length, 4);
        Assert.assertEquals(1, BytesUtils.bytesToInt(buf, 0));
    }


    @Test
    public void testPadBytes() throws Exception {
        int TOTAL_LENGTH = 20;
        int TEST_SIZE = 10;
        Buffer buffer = new AutomaticBuffer(10);
        byte[] test = new byte[10];

        random.nextBytes(test);

        buffer.putPadBytes(test, TOTAL_LENGTH);

        byte[] result = buffer.getBuffer();
        org.junit.Assert.assertEquals(result.length, TOTAL_LENGTH);
        org.junit.Assert.assertTrue("check data", Arrays.equals(Arrays.copyOfRange(test, 0, TEST_SIZE), Arrays.copyOfRange(result, 0, TEST_SIZE)));
        byte[] padBytes = new byte[TOTAL_LENGTH - TEST_SIZE];
        org.junit.Assert.assertTrue("check pad", Arrays.equals(Arrays.copyOfRange(padBytes, 0, TEST_SIZE), Arrays.copyOfRange(result, TEST_SIZE, TOTAL_LENGTH)));

    }

    @Test
    public void testPadBytes_Error() throws Exception {

        Buffer buffer1_1 = new AutomaticBuffer(32);
        try {
            buffer1_1.putPadBytes(new byte[11], 10);
            Assert.fail("error");
        } catch (IndexOutOfBoundsException ignore) {
        }

        Buffer buffer1_2 = new AutomaticBuffer(32);
        try {
            buffer1_2.putPadBytes(new byte[20], 10);
            Assert.fail("error");
        } catch (IndexOutOfBoundsException ignore) {
        }

        Buffer buffer2 = new AutomaticBuffer(32);
        buffer2.putPadBytes(new byte[10], 10);


        Buffer buffer3 = new AutomaticBuffer(5);
        buffer3.putPadBytes(new byte[10], 10);
    }


    @Test
    public void testPadString() throws Exception {
        int TOTAL_LENGTH = 20;
        int TEST_SIZE = 10;
        int PAD_SIZE = TOTAL_LENGTH - TEST_SIZE;
        Buffer buffer = new AutomaticBuffer(32);
        String test = StringUtils.repeat('a', TEST_SIZE);

        buffer.putPadString(test, TOTAL_LENGTH);

        byte[] result = buffer.getBuffer();
        String decodedString = new String(result);
        String trimString = decodedString.trim();
        Assert.assertEquals(result.length, TOTAL_LENGTH);

        Assert.assertEquals("check data", test, trimString);

        String padString = new String(result, TOTAL_LENGTH - TEST_SIZE, PAD_SIZE, Charsets.UTF_8);
        byte[] padBytes = new byte[TOTAL_LENGTH - TEST_SIZE];
        org.junit.Assert.assertEquals("check pad", padString, new String(padBytes, Charsets.UTF_8));

    }

    @Test
    public void testPadString_Error() throws Exception {

        Buffer buffer1_1 = new AutomaticBuffer(32);
        try {
            buffer1_1.putPadString(StringUtils.repeat('a', 11), 10);
            Assert.fail("error");
        } catch (IndexOutOfBoundsException ignore) {
        }

        Buffer buffer1_2 = new AutomaticBuffer(32);
        try {
            buffer1_2.putPadString(StringUtils.repeat('a', 20), 10);
            Assert.fail("error");
        } catch (Exception ignore) {
        }

        Buffer buffer2 = new AutomaticBuffer(32);
        buffer2.putPadString(StringUtils.repeat('a', 10), 10);

        Buffer buffer3 = new AutomaticBuffer(5);
        buffer3.putPadString(StringUtils.repeat('a', 10), 10);
    }

    @Test
    public void testPut2PrefixedBytes() throws Exception {
        byte[] bytes1 = new byte[2];
        checkPut2PrefixedBytes(bytes1);

        byte[] bytes2 = new byte[0];
        checkPut2PrefixedBytes(bytes2);

        byte[] bytes3 = new byte[Short.MAX_VALUE];
        checkPut2PrefixedBytes(bytes3);

        checkPut2PrefixedBytes(null);

        try {
            byte[] bytes4 = new byte[Short.MAX_VALUE+1];
            checkPut2PrefixedBytes(bytes4);
            Assert.fail("too large bytes");
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    private void checkPut2PrefixedBytes(byte[] bytes) {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.put2PrefixedBytes(bytes);

        Buffer copy = new FixedBuffer(buffer.getBuffer());
        Assert.assertArrayEquals(bytes, copy.read2PrefixedBytes());
    }

    @Test
    public void testPut4PrefixedBytes() throws Exception {
        byte[] bytes1 = new byte[2];
        checkPut4PrefixedBytes(bytes1);

        byte[] bytes2 = new byte[0];
        checkPut4PrefixedBytes(bytes2);

        checkPut4PrefixedBytes(null);

    }

    private void checkPut4PrefixedBytes(byte[] bytes) {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.put4PrefixedBytes(bytes);

        Buffer copy = new FixedBuffer(buffer.getBuffer());
        Assert.assertArrayEquals(bytes, copy.read4PrefixedBytes());
    }

    @Test
    public void testPutPrefixedBytesCheckRange() throws Exception {
        Buffer buffer = new AutomaticBuffer(1);
        buffer.putPrefixedString(null);
        byte[] internalBuffer = buffer.getInternalBuffer();
        Assert.assertEquals(1, internalBuffer.length);
    }



    @Test
    public void testCurrentTime() throws InterruptedException {
        Buffer buffer = new AutomaticBuffer(32);

        long l = System.currentTimeMillis();
        buffer.putSVLong(l);
        logger.trace("currentTime size:{}", buffer.getOffset());
        buffer.setOffset(0);
        Assert.assertEquals(buffer.readSVLong(), l);


    }

    @Test
     public void testPutVInt() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.putVInt(Integer.MAX_VALUE);
        buffer.putVInt(Integer.MIN_VALUE);
        buffer.putVInt(0);
        buffer.putVInt(1);
        buffer.putVInt(12345);

        buffer.setOffset(0);
        Assert.assertEquals(buffer.readVInt(), Integer.MAX_VALUE);
        Assert.assertEquals(buffer.readVInt(), Integer.MIN_VALUE);
        Assert.assertEquals(buffer.readVInt(), 0);
        Assert.assertEquals(buffer.readVInt(), 1);
        Assert.assertEquals(buffer.readVInt(), 12345);
    }

    @Test
    public void testPutVLong() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.putVLong(Long.MAX_VALUE);
        buffer.putVLong(Long.MIN_VALUE);
        buffer.putVLong(0L);
        buffer.putVLong(1L);
        buffer.putVLong(12345L);

        buffer.setOffset(0);
        Assert.assertEquals(buffer.readVLong(), Long.MAX_VALUE);
        Assert.assertEquals(buffer.readVLong(), Long.MIN_VALUE);
        Assert.assertEquals(buffer.readVLong(), 0L);
        Assert.assertEquals(buffer.readVLong(), 1L);
        Assert.assertEquals(buffer.readVLong(), 12345L);
    }

    @Test
    public void testPutSVLong() throws Exception {
        Buffer buffer = new AutomaticBuffer(32);
        buffer.putSVLong(Long.MAX_VALUE);
        buffer.putSVLong(Long.MIN_VALUE);
        buffer.putSVLong(0L);
        buffer.putSVLong(1L);
        buffer.putSVLong(12345L);

        buffer.setOffset(0);
        Assert.assertEquals(buffer.readSVLong(), Long.MAX_VALUE);
        Assert.assertEquals(buffer.readSVLong(), Long.MIN_VALUE);
        Assert.assertEquals(buffer.readSVLong(), 0L);
        Assert.assertEquals(buffer.readSVLong(), 1L);
        Assert.assertEquals(buffer.readSVLong(), 12345L);
    }

    @Test
    public void testPutSVInt() throws Exception {
        Buffer buffer = new AutomaticBuffer(32);
        buffer.putSVInt(Integer.MAX_VALUE);
        buffer.putSVInt(Integer.MIN_VALUE);
        buffer.putSVInt(0);
        buffer.putSVInt(1);
        buffer.putSVInt(12345);

        buffer.setOffset(0);
        Assert.assertEquals(buffer.readSVInt(), Integer.MAX_VALUE);
        Assert.assertEquals(buffer.readSVInt(), Integer.MIN_VALUE);
        Assert.assertEquals(buffer.readSVInt(), 0);
        Assert.assertEquals(buffer.readSVInt(), 1);
        Assert.assertEquals(buffer.readSVInt(), 12345);
    }

    @Test
    public void testPut() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.putInt(1);
        buffer.putLong(1L);
        buffer.putPrefixedBytes(new byte[10]);
        buffer.putByte((byte)1);


    }

    @Test
    public void testUdp() throws Exception {
        // Signature:Header{signature=85, version=100, type=28704}
        Buffer buffer = new AutomaticBuffer(10);
        // l4 Udp check payload
        buffer.putByte((byte)85);
        buffer.putByte((byte) 100);
        buffer.putShort((short)28704);

        Buffer read = new FixedBuffer(buffer.getBuffer());
        logger.debug("{}", (char)read.readByte());
        logger.debug("{}", (char)read.readByte());
        logger.debug("{}", (char)read.readByte());
        logger.debug("{}", (char)read.readByte());

    }

    @Test
    public void testRemaining() throws Exception {
        final byte[] bytes = new byte[BytesUtils.INT_BYTE_LENGTH];
        Buffer buffer = new AutomaticBuffer(bytes);
        Assert.assertEquals(buffer.remaining(), 4);
        Assert.assertTrue(buffer.hasRemaining());

        buffer.putInt(1234);
        Assert.assertEquals(buffer.remaining(), 0);
        Assert.assertFalse(buffer.hasRemaining());

        // auto expanded buffer size
        buffer.putShort((short)12);
        // remaining size increment is right operation??
        Assert.assertTrue(buffer.remaining() > 0);
        Assert.assertTrue(buffer.hasRemaining());

    }


    @Test
    public void testExpendMultiplier_2multiplier() throws Exception {
        int bufferSize = 4;
        Buffer buffer = new AutomaticBuffer(bufferSize);

        buffer.putBytes(new byte[8]);
        logger.debug("bufferSize:{} offset:{}", buffer.getInternalBuffer().length, buffer.getOffset());
        Assert.assertEquals(buffer.getInternalBuffer().length, 8);

        buffer.putBytes(new byte[8]);

        logger.debug("bufferSize:{} offset:{}", buffer.getInternalBuffer().length, buffer.getOffset());
        Assert.assertEquals(buffer.getInternalBuffer().length, 16);


        buffer.putBytes(new byte[8]);
        logger.debug("bufferSize:{} offset:{}", buffer.getInternalBuffer().length, buffer.getOffset());
        Assert.assertEquals(buffer.getInternalBuffer().length, 32);
    }

    @Test
    public void testExpendMultiplier_4multiplier() throws Exception {
        int bufferSize = 4;
        Buffer buffer = new AutomaticBuffer(bufferSize);

        buffer.putBytes(new byte[5*4]);

        logger.debug("bufferSize:{} offset:{}", buffer.getInternalBuffer().length, buffer.getOffset());
        Assert.assertEquals(buffer.getInternalBuffer().length, 32);

        buffer.putBytes(new byte[8*4]);

        logger.debug("bufferSize:{} offset:{}", buffer.getInternalBuffer().length, buffer.getOffset());
        Assert.assertEquals(buffer.getInternalBuffer().length, 64);


    }

}
