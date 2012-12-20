package com.profiler.common.bo;

import com.profiler.common.util.Buffer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class AnnotationBoTest {
    @Test
    public void testGetVersion() throws Exception {

    }

    @Test
    public void testSetVersion() throws Exception {

    }

    @Test
    public void testWriteValue() throws Exception {
        AnnotationBo bo = new AnnotationBo();
        bo.setKey("test");
        bo.setByteValue("value".getBytes("UTF-8"));
        int bufferSize = bo.getBufferSize();

        Buffer buffer = new Buffer(bufferSize);
        bo.writeValue(buffer);

        AnnotationBo bo2 = new AnnotationBo();
        bo2.readValue(buffer.getBuffer(), 0);
        Assert.assertEquals(bo.getKey(), bo2.getKey());
        Assert.assertEquals(bo.getValueType(), bo2.getValueType());
        Assert.assertArrayEquals(bo.getByteValue(), bo2.getByteValue());
    }

}
