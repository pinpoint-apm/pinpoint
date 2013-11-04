package com.nhn.pinpoint.common.buffer;

import com.nhn.pinpoint.common.util.BytesUtils;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class AutomaticBufferTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testPutPrefixedBytes() throws Exception {
        Buffer buffer = new AutomaticBuffer(0);
        buffer.put(1);
        byte[] buf = buffer.getBuffer();
        Assert.assertEquals(buf.length, 4);
        Assert.assertEquals(1, BytesUtils.bytesToInt(buf, 0));
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
        logger.info("currentTime size:{}", buffer.getOffset());
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

}
