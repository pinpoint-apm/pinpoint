package com.profiler.common.bo;

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
        bo.setValue("value".getBytes("UTF-8"));
        int bufferSize = bo.getBufferSize();
        byte[] bytes = new byte[bufferSize];
        bo.writeValue(bytes, 0);

        AnnotationBo bo2 = new AnnotationBo();
        bo2.readValue(bytes, 0);
        Assert.assertEquals(bo.getKey(), bo2.getKey());
        Assert.assertEquals(bo.getTimestamp(), bo2.getTimestamp());
        Assert.assertEquals(bo.getValueType(), bo2.getValueType());
        Assert.assertArrayEquals(bo.getValue(), bo2.getValue());
    }

}
