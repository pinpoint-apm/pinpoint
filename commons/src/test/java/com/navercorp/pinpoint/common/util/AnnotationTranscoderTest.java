package com.nhn.pinpoint.common.util;


import com.nhn.pinpoint.common.bo.IntStringValue;
import com.nhn.pinpoint.thrift.dto.TIntStringValue;
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
 * @author emeroad
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

        byte typeCode = transcoder.getTypeCode(value);
        byte[] bytes = transcoder.encode(value, typeCode);
        Object decode = transcoder.decode(typeCode, bytes);

        Assert.assertEquals(value, decode);
    }

    private void typeUnsupportCode(Object value) {
        AnnotationTranscoder transcoder = new AnnotationTranscoder();

        byte typeCode = transcoder.getTypeCode(value);
        byte[] bytes = transcoder.encode(value, typeCode);
        Object decode = transcoder.decode(typeCode, bytes);

        Assert.assertEquals(value.toString(), decode.toString());
    }

    private void typeBinaryCode(byte[] value) {
        AnnotationTranscoder transcoder = new AnnotationTranscoder();

        byte typeCode = transcoder.getTypeCode(value);
        byte[] bytes = transcoder.encode(value, typeCode);
        Object decode = transcoder.decode(typeCode, bytes);

        Assert.assertArrayEquals(value, (byte[]) decode);
    }

    @Test
    public void testGetTypeCode() throws Exception {
        int i = 2 << 8;
        logger.debug("{}", i);
        write(i);
        int j = 3 << 8;
        logger.debug("{}", j);
        write(j);
        write(10);
        write(512);
        write(256);


    }

    @Test
    public void testIntString() {

        testIntString(-1, "");
        testIntString(0, "");
        testIntString(1, "");
        testIntString(Integer.MAX_VALUE, "test");
        testIntString(Integer.MIN_VALUE, "test");

        // null일때 0인자열로 생각하는 문제점이 있음.
        testIntString(2, null);
    }

    private void testIntString(int intValue, String stringValue) {
        AnnotationTranscoder transcoder = new AnnotationTranscoder();
        TIntStringValue tIntStringValue = new TIntStringValue(intValue);
        tIntStringValue.setStringValue(stringValue);
        byte[] encode = transcoder.encode(tIntStringValue, AnnotationTranscoder.CODE_INT_STRING);
        IntStringValue decode = (IntStringValue) transcoder.decode(AnnotationTranscoder.CODE_INT_STRING, encode);
        Assert.assertEquals(tIntStringValue.getIntValue(), decode.getIntValue());
        Assert.assertEquals(tIntStringValue.getStringValue(), decode.getStringValue());
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
