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

import org.junit.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author emeroad
 */
public class OffsetFixedBufferTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testFixedBuffer() throws Exception {
        new OffsetFixedBuffer(new byte[10], 10, 0);
        try {
            new OffsetFixedBuffer(new byte[10], 11, 0);
            Assert.fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
        try {
            new OffsetFixedBuffer(new byte[10], -1, 0);
            Assert.fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    @Test
    public void testFixedBuffer_length() throws Exception {
        try {
            new OffsetFixedBuffer(new byte[10], 0, 11);
            Assert.fail();
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }

        new OffsetFixedBuffer(new byte[10], 0, 10);

    }

    @Test
    public void testGetBuffer() throws Exception {
        final int bufferSize = 10;
        Buffer buffer = new OffsetFixedBuffer(new byte[bufferSize], 2, bufferSize - 2);
        final int putValue = 10;
        buffer.putInt(putValue);
        byte[] intBuffer = buffer.getBuffer();
        Assert.assertEquals(intBuffer.length, 4);

        Buffer read = new FixedBuffer(intBuffer);
        int value = read.readInt();
        Assert.assertEquals(putValue, value);
    }

    @Test
    public void testCopyBuffer() throws Exception {
        final int bufferSize = 10;
        Buffer buffer = new OffsetFixedBuffer(new byte[bufferSize], 2, bufferSize - 2);

        final int putValue = 10;
        buffer.putInt(putValue);
        byte[] intBuffer = buffer.copyBuffer();
        Assert.assertEquals(intBuffer.length, 4);

        Buffer read = new FixedBuffer(intBuffer);
        int value = read.readInt();
        Assert.assertEquals(putValue, value);
    }

    @Test
    public void testWrapByteBuffer() throws Exception {
        final int bufferSize = 10;
        Buffer buffer = new OffsetFixedBuffer(new byte[bufferSize], 2, bufferSize - 2);

        buffer.putInt(1);
        buffer.putInt(2);

        ByteBuffer byteBuffer = buffer.wrapByteBuffer();
        Assert.assertEquals(1, byteBuffer.getInt());
        Assert.assertEquals(2, byteBuffer.getInt());
    }

}
