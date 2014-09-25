package com.nhn.pinpoint.common.buffer;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class OffsetFixedBufferTest {

    @Test
    public void testFixedBuffer() throws Exception {
        new OffsetFixedBuffer(new byte[10], 10);
        try {
            new OffsetFixedBuffer(new byte[10], 11);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            new OffsetFixedBuffer(new byte[10], -1);
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testGetBuffer() throws Exception {
        final int putValue = 10;
        Buffer buffer = new OffsetFixedBuffer(new byte[10], 2);
        buffer.put(putValue);
        byte[] intBuffer = buffer.getBuffer();
        Assert.assertEquals(intBuffer.length, 4);

        Buffer read = new FixedBuffer(intBuffer);
        int value = read.readInt();
        Assert.assertEquals(putValue, value);
    }
}
