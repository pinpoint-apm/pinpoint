package com.profiler.common.util;


import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

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
        int i = 2 << 8;
        System.out.println(i);
        write(i);
        int j = 3 << 8;
        System.out.println(j);
        write(j);
        write(10);
        write(512);
        write(256);



    }

    private void write(int value) throws TException {
        TCompactProtocol.Factory factory = new TCompactProtocol.Factory();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
        TIOStreamTransport transport = new TIOStreamTransport(baos);
        TProtocol protocol = factory.getProtocol(transport);

        protocol.writeI32(value);
        byte[] buffer = baos.toByteArray();
        logger.info(Arrays.toString(buffer));
    }

    @Test
    public void testEncode() throws Exception {

    }
}
