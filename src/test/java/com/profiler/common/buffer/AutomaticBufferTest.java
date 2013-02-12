package com.profiler.common.buffer;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 */
public class AutomaticBufferTest {

    @Test
    public void testPut1PrefixedBytes() throws Exception {

    }

    @Test
    public void testPutPrefixedBytes() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.put(1);
        byte[] buf = buffer.getBuffer();
        Assert.assertEquals(buf.length, 4);
    }

    @Test
    public void testPutNullTerminatedBytes() throws Exception {

    }

    @Test
    public void testPut() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.put(1);
        buffer.put(1L);
        buffer.putPrefixedBytes(new byte[10]);
        buffer.put((byte)1);


    }

}
