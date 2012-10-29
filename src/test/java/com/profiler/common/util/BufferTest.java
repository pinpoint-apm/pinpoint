package com.profiler.common.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 */
public class BufferTest {
    @Test
    public void testPutPrefixedBytes() throws Exception {
        String test = "test";
        int expected = 3333;

        Buffer buffer = new Buffer(1024);
        buffer.putPrefixedBytes(test.getBytes("UTF-8"));

        buffer.put(expected);
        byte[] buffer1 = buffer.getBuffer();

        Buffer actual = new Buffer(buffer1);
        String s = actual.readPrefixedString();
        Assert.assertEquals(test, s);

        int i = actual.readInt();
        Assert.assertEquals(expected, i);


    }

    @Test
    public void testReadByte() throws Exception {

    }

    @Test
    public void testReadBoolean() throws Exception {

    }

    @Test
    public void testReadInt() throws Exception {

    }

    @Test
    public void testReadLong() throws Exception {

    }

    @Test
    public void testReadPrefixedBytes() throws Exception {

    }

    @Test
    public void testReadPrefixedString() throws Exception {

    }

    @Test
    public void testPut() throws Exception {

    }


    @Test
    public void testGetBuffer() throws Exception {

    }

    @Test
    public void testGetOffset() throws Exception {

    }
}
