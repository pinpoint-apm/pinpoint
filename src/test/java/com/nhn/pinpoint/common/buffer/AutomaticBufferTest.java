package com.nhn.pinpoint.common.buffer;

import com.nhn.pinpoint.common.util.BytesUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class AutomaticBufferTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testPutPrefixedBytes() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.put(1);
        byte[] buf = buffer.getBuffer();
        Assert.assertEquals(buf.length, 4);
        Assert.assertEquals(1, BytesUtils.bytesToInt(buf, 0));
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
        } catch (Exception e) {
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
        // 상속 관계에 의해서 강제로 버퍼 사이즈를 늘어나지 않아도 되는데 사이즈가 늘어남.
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

    @Ignore
    @Test
    public void testUdp() throws Exception {
        // Signature:Header{signature=85, version=100, type=28704}
        Buffer buffer = new AutomaticBuffer(10);
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
