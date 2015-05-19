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

import com.navercorp.pinpoint.common.util.BytesUtils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
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
        buffer.put(1);
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

        String padString = new String(result, TOTAL_LENGTH - TEST_SIZE, PAD_SIZE, "UTF-8");
        byte[] padBytes = new byte[TOTAL_LENGTH - TEST_SIZE];
        org.junit.Assert.assertEquals("check pad", padString, new String(padBytes, Charset.forName("UTF-8")));

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
        buffer.putSVar(l);
        logger.trace("currentTime size:{}", buffer.getOffset());
        buffer.setOffset(0);
        Assert.assertEquals(buffer.readSVarLong(), l);


    }

    @Test
     public void testPutVarInt() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.putVar(Integer.MAX_VALUE);
        buffer.putVar(Integer.MIN_VALUE);
        buffer.putVar(0);
        buffer.putVar(1);
        buffer.putVar(12345);

        buffer.setOffset(0);
        Assert.assertEquals(buffer.readVarInt(), Integer.MAX_VALUE);
        Assert.assertEquals(buffer.readVarInt(), Integer.MIN_VALUE);
        Assert.assertEquals(buffer.readVarInt(), 0);
        Assert.assertEquals(buffer.readVarInt(), 1);
        Assert.assertEquals(buffer.readVarInt(), 12345);
    }

    @Test
    public void testPutVarLong() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.putVar(Long.MAX_VALUE);
        buffer.putVar(Long.MIN_VALUE);
        buffer.putVar(0L);
        buffer.putVar(1L);
        buffer.putVar(12345L);

        buffer.setOffset(0);
        Assert.assertEquals(buffer.readVarLong(), Long.MAX_VALUE);
        Assert.assertEquals(buffer.readVarLong(), Long.MIN_VALUE);
        Assert.assertEquals(buffer.readVarLong(), 0L);
        Assert.assertEquals(buffer.readVarLong(), 1L);
        Assert.assertEquals(buffer.readVarLong(), 12345L);
    }

    @Test
    public void testPutSVarLong() throws Exception {
        Buffer buffer = new AutomaticBuffer(32);
        buffer.putSVar(Long.MAX_VALUE);
        buffer.putSVar(Long.MIN_VALUE);
        buffer.putSVar(0L);
        buffer.putSVar(1L);
        buffer.putSVar(12345L);

        buffer.setOffset(0);
        Assert.assertEquals(buffer.readSVarLong(), Long.MAX_VALUE);
        Assert.assertEquals(buffer.readSVarLong(), Long.MIN_VALUE);
        Assert.assertEquals(buffer.readSVarLong(), 0L);
        Assert.assertEquals(buffer.readSVarLong(), 1L);
        Assert.assertEquals(buffer.readSVarLong(), 12345L);
    }

    @Test
    public void testPutSVarInt() throws Exception {
        Buffer buffer = new AutomaticBuffer(32);
        buffer.putSVar(Integer.MAX_VALUE);
        buffer.putSVar(Integer.MIN_VALUE);
        buffer.putSVar(0);
        buffer.putSVar(1);
        buffer.putSVar(12345);

        buffer.setOffset(0);
        Assert.assertEquals(buffer.readSVarInt(), Integer.MAX_VALUE);
        Assert.assertEquals(buffer.readSVarInt(), Integer.MIN_VALUE);
        Assert.assertEquals(buffer.readSVarInt(), 0);
        Assert.assertEquals(buffer.readSVarInt(), 1);
        Assert.assertEquals(buffer.readSVarInt(), 12345);
    }

    @Test
    public void testPut() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.put(1);
        buffer.put(1L);
        buffer.putPrefixedBytes(new byte[10]);
        buffer.put((byte)1);


    }

    @Test
    public void testUdp() throws Exception {
        // Signature:Header{signature=85, version=100, type=28704}
        Buffer buffer = new AutomaticBuffer(10);
        // l4 Udp check payload
        buffer.put((byte)85);
        buffer.put((byte) 100);
        buffer.put((short)28704);

        Buffer read = new FixedBuffer(buffer.getBuffer());
        logger.info("{}", (char)read.readByte());
        logger.info("{}", (char)read.readByte());
        logger.info("{}", (char)read.readByte());
        logger.info("{}", (char)read.readByte());

    }

}
