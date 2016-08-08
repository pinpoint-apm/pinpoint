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
public class OffsetAutomaticBufferTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testGetBuffer() throws Exception {
        final int bufferSize = 10;
        byte[] byteArray = new byte[bufferSize];
        Buffer buffer = new OffsetAutomaticBuffer(byteArray, 2, byteArray.length - 2);

        final int putValue = 10;
        buffer.putInt(putValue);
        byte[] intBuffer = buffer.getBuffer();
        Assert.assertNotSame("deepcopy", intBuffer, byteArray);
        Assert.assertEquals(intBuffer.length, 4);

        Buffer read = new FixedBuffer(intBuffer);
        int value = read.readInt();
        Assert.assertEquals(putValue, value);
    }

    @Test
    public void testGetBuffer_shallowcopy() throws Exception {
        final int bufferSize = 4;
        byte[] byteArray = new byte[bufferSize];
        Buffer buffer = new OffsetAutomaticBuffer(byteArray);

        final int putValue = 10;
        buffer.putInt(putValue);
        byte[] intBuffer = buffer.getBuffer();
        Assert.assertSame("shallowcopy", intBuffer, byteArray);
        Assert.assertEquals(intBuffer.length, 4);

        Buffer read = new FixedBuffer(intBuffer);
        int value = read.readInt();
        Assert.assertEquals(putValue, value);
    }

    @Test
    public void testCopyBuffer() throws Exception {
        final int bufferSize = 10;
        byte[] byteArray = new byte[bufferSize];
        Buffer buffer = new OffsetAutomaticBuffer(byteArray, 2, bufferSize - 2);

        final int putValue = 10;
        buffer.putInt(putValue);
        byte[] intBuffer = buffer.copyBuffer();
        Assert.assertNotSame("deepcopy", intBuffer, byteArray);
        Assert.assertEquals(intBuffer.length, 4);

        Buffer read = new FixedBuffer(intBuffer);
        int value = read.readInt();
        Assert.assertEquals(putValue, value);
    }

    @Test
    public void testWrapByteBuffer() throws Exception {
        final int bufferSize = 10;
        byte[] byteArray = new byte[bufferSize];
        Buffer buffer = new OffsetAutomaticBuffer(byteArray, 2, bufferSize - 2);
        buffer.putInt(1);
        buffer.putInt(2);

        ByteBuffer byteBuffer = buffer.wrapByteBuffer();
        Assert.assertSame("shallowcopy", byteArray, byteBuffer.array());
        Assert.assertEquals(1, byteBuffer.getInt());
        Assert.assertEquals(2, byteBuffer.getInt());
    }


    @Test
    public void testCheckExpand() throws Exception {
        final int bufferSize = 4;
        int startOffset = 2;
        Buffer buffer = new OffsetAutomaticBuffer(new byte[bufferSize], startOffset, bufferSize - startOffset);
        Assert.assertEquals("remaining", buffer.remaining(), 2);
        buffer.putInt(1);
        buffer.putInt(2);

        int remaining = buffer.remaining();
        logger.debug("remaining:{}", remaining);

        ByteBuffer byteBuffer = buffer.wrapByteBuffer();
        Assert.assertEquals(1, byteBuffer.getInt());
        Assert.assertEquals(2, byteBuffer.getInt());

    }

    @Test
    public void testCheckExpand_test() throws Exception {
        final int bufferSize = 4;
        Buffer buffer = new OffsetAutomaticBuffer(new byte[bufferSize], 0, bufferSize);
        Assert.assertEquals("remaining", buffer.remaining(), 4);
        buffer.putInt(1);
        Assert.assertEquals("remaining", buffer.remaining(), 0);
        buffer.putInt(2);

        int remaining = buffer.remaining();
        logger.debug("remaining:{}", remaining);

        ByteBuffer byteBuffer = buffer.wrapByteBuffer();
        Assert.assertEquals(1, byteBuffer.getInt());
        Assert.assertEquals(2, byteBuffer.getInt());

    }
}
