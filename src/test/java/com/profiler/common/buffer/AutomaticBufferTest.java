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
    public void testPut2PrefixedBytes() throws Exception {
        int zigzag1 = zigzag1(9);
        System.out.println(zigzag1);
        int zigzag = zigzag1(10);
        System.out.println(zigzag);
    }

    private int zigzag1(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    private int zigzag(int i) {
        return (i<<1) ^ (i>>31);
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
