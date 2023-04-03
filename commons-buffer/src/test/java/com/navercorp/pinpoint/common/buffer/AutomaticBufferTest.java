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

package com.navercorp.pinpoint.common.buffer;

import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author emeroad
 */
public class AutomaticBufferTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Random random = new Random();

    @Test
    public void testPutPrefixedBytes() {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.putInt(1);
        byte[] buf = buffer.getBuffer();
        assertThat(buf).hasSize(4);
        Assertions.assertEquals(1, BytesUtils.bytesToInt(buf, 0));
    }


    @Test
    public void testPadBytes() {
        int TOTAL_LENGTH = 20;
        int TEST_SIZE = 10;
        Buffer buffer = new AutomaticBuffer(10);
        byte[] test = new byte[10];

        random.nextBytes(test);

        buffer.putPadBytes(test, TOTAL_LENGTH);

        byte[] result = buffer.getBuffer();
        assertThat(result).hasSize(TOTAL_LENGTH);
        Assertions.assertArrayEquals(Arrays.copyOfRange(test, 0, TEST_SIZE), Arrays.copyOfRange(result, 0, TEST_SIZE), "check data");
        byte[] padBytes = new byte[TOTAL_LENGTH - TEST_SIZE];
        Assertions.assertArrayEquals(Arrays.copyOfRange(padBytes, 0, TEST_SIZE), Arrays.copyOfRange(result, TEST_SIZE, TOTAL_LENGTH), "check pad");

    }

    @Test
    public void testPadBytes_Error() {

        Buffer buffer1_1 = new AutomaticBuffer(32);
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            buffer1_1.putPadBytes(new byte[11], 10);
        });

        Buffer buffer1_2 = new AutomaticBuffer(32);
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            buffer1_2.putPadBytes(new byte[20], 10);
        });

        Buffer buffer2 = new AutomaticBuffer(32);
        buffer2.putPadBytes(new byte[10], 10);


        Buffer buffer3 = new AutomaticBuffer(5);
        buffer3.putPadBytes(new byte[10], 10);
    }


    @Test
    public void testPadString() {
        int TOTAL_LENGTH = 20;
        int TEST_SIZE = 10;
        int PAD_SIZE = TOTAL_LENGTH - TEST_SIZE;
        Buffer buffer = new AutomaticBuffer(32);
        String test = StringUtils.repeat("a", TEST_SIZE);

        buffer.putPadString(test, TOTAL_LENGTH);

        byte[] result = buffer.getBuffer();
        String decodedString = new String(result);
        String trimString = decodedString.trim();
        assertThat(result).hasSize(TOTAL_LENGTH);

        Assertions.assertEquals(test, trimString, "check data");

        String padString = new String(result, TOTAL_LENGTH - TEST_SIZE, PAD_SIZE, StandardCharsets.UTF_8);
        byte[] padBytes = new byte[TOTAL_LENGTH - TEST_SIZE];
        Assertions.assertEquals(padString, new String(padBytes, StandardCharsets.UTF_8), "check pad");

    }

    @Test
    public void testPadString_Error() {

        Buffer buffer1_1 = new AutomaticBuffer(32);
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            buffer1_1.putPadString(StringUtils.repeat("a", 11), 10);
        });

        Buffer buffer1_2 = new AutomaticBuffer(32);
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            buffer1_2.putPadString(StringUtils.repeat("a", 20), 10);
        });

        Buffer buffer2 = new AutomaticBuffer(32);
        buffer2.putPadString(StringUtils.repeat("a", 10), 10);

        Buffer buffer3 = new AutomaticBuffer(5);
        buffer3.putPadString(StringUtils.repeat("a", 10), 10);
    }

    @Test
    public void testPut2PrefixedBytes() {
        byte[] bytes1 = new byte[2];
        checkPut2PrefixedBytes(bytes1);

        byte[] bytes2 = new byte[0];
        checkPut2PrefixedBytes(bytes2);

        byte[] bytes3 = new byte[Short.MAX_VALUE];
        checkPut2PrefixedBytes(bytes3);

        checkPut2PrefixedBytes(null);

        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            byte[] bytes4 = new byte[Short.MAX_VALUE + 1];
            checkPut2PrefixedBytes(bytes4);
        });
    }

    private void checkPut2PrefixedBytes(byte[] bytes) {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.put2PrefixedBytes(bytes);

        Buffer copy = new FixedBuffer(buffer.getBuffer());
        assertThat(bytes).isEqualTo(copy.read2PrefixedBytes());
    }

    @Test
    public void testPut4PrefixedBytes() {
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
        assertThat(bytes).isEqualTo(copy.read4PrefixedBytes());
    }

    @Test
    public void testPutPrefixedBytesCheckRange() {
        Buffer buffer = new AutomaticBuffer(1);
        buffer.putPrefixedString(null);
        byte[] internalBuffer = buffer.getInternalBuffer();
        assertThat(internalBuffer).hasSize(1);
    }


    @Test
    public void testCurrentTime() {
        Buffer buffer = new AutomaticBuffer(32);

        long l = System.currentTimeMillis();
        buffer.putSVLong(l);
        logger.trace("currentTime size:{}", buffer.getOffset());
        buffer.setOffset(0);
        Assertions.assertEquals(buffer.readSVLong(), l);


    }

    @Test
    public void testPutVInt() {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.putVInt(Integer.MAX_VALUE);
        buffer.putVInt(Integer.MIN_VALUE);
        buffer.putVInt(0);
        buffer.putVInt(1);
        buffer.putVInt(12345);

        buffer.setOffset(0);
        Assertions.assertEquals(buffer.readVInt(), Integer.MAX_VALUE);
        Assertions.assertEquals(buffer.readVInt(), Integer.MIN_VALUE);
        Assertions.assertEquals(buffer.readVInt(), 0);
        Assertions.assertEquals(buffer.readVInt(), 1);
        Assertions.assertEquals(buffer.readVInt(), 12345);
    }

    @Test
    public void testPutVLong() {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.putVLong(Long.MAX_VALUE);
        buffer.putVLong(Long.MIN_VALUE);
        buffer.putVLong(0L);
        buffer.putVLong(1L);
        buffer.putVLong(12345L);

        buffer.setOffset(0);
        Assertions.assertEquals(buffer.readVLong(), Long.MAX_VALUE);
        Assertions.assertEquals(buffer.readVLong(), Long.MIN_VALUE);
        Assertions.assertEquals(buffer.readVLong(), 0L);
        Assertions.assertEquals(buffer.readVLong(), 1L);
        Assertions.assertEquals(buffer.readVLong(), 12345L);
    }

    @Test
    public void testPutSVLong() {
        Buffer buffer = new AutomaticBuffer(32);
        buffer.putSVLong(Long.MAX_VALUE);
        buffer.putSVLong(Long.MIN_VALUE);
        buffer.putSVLong(0L);
        buffer.putSVLong(1L);
        buffer.putSVLong(12345L);

        buffer.setOffset(0);
        Assertions.assertEquals(buffer.readSVLong(), Long.MAX_VALUE);
        Assertions.assertEquals(buffer.readSVLong(), Long.MIN_VALUE);
        Assertions.assertEquals(buffer.readSVLong(), 0L);
        Assertions.assertEquals(buffer.readSVLong(), 1L);
        Assertions.assertEquals(buffer.readSVLong(), 12345L);
    }

    @Test
    public void testPutSVInt() {
        Buffer buffer = new AutomaticBuffer(32);
        buffer.putSVInt(Integer.MAX_VALUE);
        buffer.putSVInt(Integer.MIN_VALUE);
        buffer.putSVInt(0);
        buffer.putSVInt(1);
        buffer.putSVInt(12345);

        buffer.setOffset(0);
        Assertions.assertEquals(buffer.readSVInt(), Integer.MAX_VALUE);
        Assertions.assertEquals(buffer.readSVInt(), Integer.MIN_VALUE);
        Assertions.assertEquals(buffer.readSVInt(), 0);
        Assertions.assertEquals(buffer.readSVInt(), 1);
        Assertions.assertEquals(buffer.readSVInt(), 12345);
    }

    @Test
    public void testPut() {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.putInt(1);
        buffer.putLong(1L);
        buffer.putPrefixedBytes(new byte[10]);
        buffer.putByte((byte) 1);


    }

    @Test
    public void testUdp() {
        // Signature:Header{signature=85, version=100, type=28704}
        Buffer buffer = new AutomaticBuffer(10);
        // l4 Udp check payload
        buffer.putByte((byte) 85);
        buffer.putByte((byte) 100);
        buffer.putShort((short) 28704);

        Buffer read = new FixedBuffer(buffer.getBuffer());
        logger.debug("{}", (char) read.readByte());
        logger.debug("{}", (char) read.readByte());
        logger.debug("{}", (char) read.readByte());
        logger.debug("{}", (char) read.readByte());

    }

    @Test
    public void testRemaining() {
        final byte[] bytes = new byte[BytesUtils.INT_BYTE_LENGTH];
        Buffer buffer = new AutomaticBuffer(bytes);
        Assertions.assertEquals(buffer.remaining(), 4);
        Assertions.assertTrue(buffer.hasRemaining());

        buffer.putInt(1234);
        Assertions.assertEquals(buffer.remaining(), 0);
        Assertions.assertFalse(buffer.hasRemaining());

        // auto expanded buffer size
        buffer.putShort((short) 12);
        // remaining size increment is right operation??
        Assertions.assertTrue(buffer.remaining() > 0);
        Assertions.assertTrue(buffer.hasRemaining());

    }


    @Test
    public void testExpendMultiplier_2multiplier() {
        int bufferSize = 4;
        Buffer buffer = new AutomaticBuffer(bufferSize);

        buffer.putBytes(new byte[8]);
        logger.debug("bufferSize:{} offset:{}", buffer.getInternalBuffer().length, buffer.getOffset());
        assertThat(buffer.getInternalBuffer()).hasSize(8);

        buffer.putBytes(new byte[8]);

        logger.debug("bufferSize:{} offset:{}", buffer.getInternalBuffer().length, buffer.getOffset());
        assertThat(buffer.getInternalBuffer()).hasSize(16);


        buffer.putBytes(new byte[8]);
        logger.debug("bufferSize:{} offset:{}", buffer.getInternalBuffer().length, buffer.getOffset());
        assertThat(buffer.getInternalBuffer()).hasSize(32);
    }

    @Test
    public void testExpendMultiplier_4multiplier() {
        int bufferSize = 4;
        Buffer buffer = new AutomaticBuffer(bufferSize);

        buffer.putBytes(new byte[5 * 4]);

        logger.debug("bufferSize:{} offset:{}", buffer.getInternalBuffer().length, buffer.getOffset());
        assertThat(buffer.getInternalBuffer()).hasSize(32);

        buffer.putBytes(new byte[8 * 4]);

        logger.debug("bufferSize:{} offset:{}", buffer.getInternalBuffer().length, buffer.getOffset());
        assertThat(buffer.getInternalBuffer()).hasSize(64);


    }

}
