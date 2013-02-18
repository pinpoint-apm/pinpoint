package com.profiler.common.util;


import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;

/**
 *
 */
public class AnnotationTranscoderTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Test
    public void testDecode() throws Exception {
        typeCode("test");
        typeCode("");
        typeCode("adfesdfsesdfsdfserfsdfsdfe");

        typeCode(1);
        typeCode(0);
        typeCode(-1212);

        typeCode((short) 4);
        typeCode((short) -124);

        typeCode(2L);
        typeCode(-22342342L);

        typeCode(3f);
        typeCode(123.3f);

        typeCode(4D);
        typeCode(-124D);

        typeCode((byte) 4);
        typeCode((byte) -14);

        typeCode(true);
        typeCode(false);

        typeCode(null);

        typeUnsupportCode(new Date());

        typeBinaryCode(new byte[]{12, 3, 4, 1, 23, 4, 1, 2, 3, 4, 4});

    }

    private void typeCode(Object value) {
        AnnotationTranscoder transcoder = new AnnotationTranscoder();

        int typeCode = transcoder.getTypeCode(value);
        byte[] bytes = transcoder.encode(value, typeCode);
        Object decode = transcoder.decode(typeCode, bytes);

        Assert.assertEquals(value, decode);
    }

    private void typeUnsupportCode(Object value) {
        AnnotationTranscoder transcoder = new AnnotationTranscoder();

        int typeCode = transcoder.getTypeCode(value);
        byte[] bytes = transcoder.encode(value, typeCode);
        Object decode = transcoder.decode(typeCode, bytes);

        Assert.assertEquals(value.toString(), decode.toString());
    }

    private void typeBinaryCode(byte[] value) {
        AnnotationTranscoder transcoder = new AnnotationTranscoder();

        int typeCode = transcoder.getTypeCode(value);
        byte[] bytes = transcoder.encode(value, typeCode);
        Object decode = transcoder.decode(typeCode, bytes);

        Assert.assertArrayEquals(value, (byte[]) decode);
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
