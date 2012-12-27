package com.profiler.common.util;


import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class AnnotationTranscoderTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Test
    public void testDecode() throws Exception {
        typeCode("test");
        typeCode(1);
        typeCode(2L);
        typeCode(3f);
        typeCode(4D);
        typeCode(true);

    }

    private void typeCode(Object value) {
        AnnotationTranscoder transcoder = new AnnotationTranscoder();
        AnnotationTranscoder.Encoded encoded = transcoder.encode(value);

        int valueType = encoded.getValueType();
        int typeCode = transcoder.getTypeCode(value);
        Assert.assertEquals(valueType, typeCode);

        byte[] bytes = transcoder.encode(value, typeCode);
        Assert.assertArrayEquals(bytes, encoded.getBytes());
    }

    @Test
    public void testGetTypeCode() throws Exception {

    }

    @Test
    public void testEncode() throws Exception {

    }
}
