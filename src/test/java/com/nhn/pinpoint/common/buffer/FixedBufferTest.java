package com.nhn.pinpoint.common.buffer;

import junit.framework.Assert;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class FixedBufferTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testPutPrefixedBytes() throws Exception {
        String test = "test";
        int expected = 3333;

        Buffer buffer = new FixedBuffer(1024);
        buffer.putPrefixedBytes(test.getBytes("UTF-8"));

        buffer.put(expected);
        byte[] buffer1 = buffer.getBuffer();

        Buffer actual = new FixedBuffer(buffer1);
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
    public void testReadPrefixedString() throws Exception {

    }

    @Test
    public void testRead4PrefixedString() throws Exception {
        String value = "test";
        byte[] length = Bytes.toBytes(value.length());
        byte[] string = Bytes.toBytes(value);
        byte[] result = Bytes.add(length, string);


        Buffer buffer = new FixedBuffer(result);
        String prefixedString = buffer.read4PrefixedString();
        Assert.assertEquals(prefixedString, value);

    }

    @Test
    public void testRead4PrefixedString_Null() throws Exception {
        byte[] length = Bytes.toBytes(-1);


        Buffer buffer = new FixedBuffer(length);
        String prefixedString = buffer.read4PrefixedString();
        Assert.assertEquals(prefixedString, null);

    }

    @Test
    public void testPut() throws Exception {
        checkUnsignedByte(255);

        checkUnsignedByte(0);
    }

    @Test
    public void testPutVar32() throws Exception {
        checkVarInt(Integer.MAX_VALUE, 5);
        checkVarInt(25, 1);
        checkVarInt(100, 1);

        checkVarInt(Integer.MIN_VALUE, 10);

        checkVarInt(0, -1);
        checkVarInt(Integer.MAX_VALUE / 2, -1);
        checkVarInt(Integer.MAX_VALUE / 10, -1);
        checkVarInt(Integer.MAX_VALUE / 10000, -1);

        checkVarInt(Integer.MIN_VALUE / 2, -1);
        checkVarInt(Integer.MIN_VALUE / 10, -1);
        checkVarInt(Integer.MIN_VALUE / 10000, -1);

    }

    private void checkVarInt(int v, int offset) {
        Buffer buffer = new FixedBuffer(32);
        buffer.putVar(v);
        if (offset != -1) {
            Assert.assertEquals(buffer.getOffset(), offset);
        } else {
            logger.info("{} offsetSize:{}", v, buffer.getOffset());
        }
        buffer.setOffset(0);
        int readV = buffer.readVarInt();
        Assert.assertEquals(readV, v);
    }

    @Test
    public void testPutSVar32() throws Exception {
        // 63이 1바이트 경계.
        checkSVarInt(63, -1);
        // 8191이 2바이트 경계
        checkSVarInt((1024*8)-1, -1);

        checkSVarInt(3, -1);

        checkSVarInt(Integer.MAX_VALUE, 5);

        checkSVarInt(Integer.MIN_VALUE, 5);

        checkSVarInt(0, -1);
        checkSVarInt(Integer.MAX_VALUE / 2, -1);
        checkSVarInt(Integer.MAX_VALUE / 10, -1);
        checkSVarInt(Integer.MAX_VALUE / 10000, -1);

        checkSVarInt(Integer.MIN_VALUE / 2, -1);
        checkSVarInt(Integer.MIN_VALUE / 10, -1);
        checkSVarInt(Integer.MIN_VALUE / 10000, -1);


    }

    private void checkSVarInt(int v, int offset) {
        Buffer buffer = new FixedBuffer(32);
        buffer.putSVar(v);
        if (offset != -1) {
            Assert.assertEquals(buffer.getOffset(), offset);
        } else {
            logger.info("{} offsetSize:{}", v, buffer.getOffset());
        }
        buffer.setOffset(0);
        int readV = buffer.readSVarInt();
        Assert.assertEquals(readV, v);
    }

    @Test
    public void testPutVar64() throws Exception {

    }

    private void checkUnsignedByte(int value) {
        Buffer buffer = new FixedBuffer(1024);
        buffer.put((byte) value);
        byte[] buffer1 = buffer.getBuffer();

        Buffer reader = new FixedBuffer(buffer1);
        int i = reader.readUnsignedByte();
        Assert.assertEquals(value, i);
    }


    @Test
    public void testGetBuffer() throws Exception {
        Buffer buffer = new FixedBuffer(4);
        buffer.put(1);
        Assert.assertEquals(buffer.getOffset(), 4);
        Assert.assertEquals(buffer.getBuffer().length, 4);
    }

    @Test
    public void testSliceGetBuffer() throws Exception {
        Buffer buffer = new FixedBuffer(5);
        buffer.put(1);
        Assert.assertEquals(buffer.getOffset(), 4);
        Assert.assertEquals(buffer.getBuffer().length, 4);

        byte[] buffer1 = buffer.getBuffer();
        byte[] buffer2 = buffer.getBuffer();
        Assert.assertTrue(buffer1 != buffer2);

    }

    @Test
    public void testBoolean() {
        Buffer buffer = new FixedBuffer(16);
        buffer.put(true);
        buffer.put(false);

        Buffer read = new FixedBuffer(buffer.getBuffer());
        boolean b = read.readBoolean();
        Assert.assertEquals(true, b);

        boolean c = read.readBoolean();
        Assert.assertEquals(false, c);
    }

    @Test
    public void testGetOffset() throws Exception {
        Buffer buffer = new FixedBuffer();
        Assert.assertEquals(buffer.getOffset(), 0);

        buffer.put(4);
        Assert.assertEquals(buffer.getOffset(), 4);

    }
}
