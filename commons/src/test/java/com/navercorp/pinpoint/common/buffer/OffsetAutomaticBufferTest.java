package com.nhn.pinpoint.common.buffer;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class OffsetAutomaticBufferTest {
    @Test
    public void testGetBuffer() throws Exception {
        final int putValue = 10;
        Buffer buffer = new OffsetAutomaticBuffer(new byte[10], 2);
        buffer.put(putValue);
        byte[] intBuffer = buffer.getBuffer();
        Assert.assertEquals(intBuffer.length, 4);

        Buffer read = new FixedBuffer(intBuffer);
        int value = read.readInt();
        Assert.assertEquals(putValue, value);
    }
}
