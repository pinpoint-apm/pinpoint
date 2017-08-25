/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;


import com.navercorp.pinpoint.thrift.dto.TIntBooleanIntBooleanValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;

import com.navercorp.pinpoint.thrift.dto.TLongIntIntByteByteStringValue;
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
        typeCode(268435455L);
        typeCode(268435456L);
        typeCode(34359738367L);
        typeCode(34359738368L);
        typeCode(Long.MAX_VALUE);
        typeCode(Long.MIN_VALUE);

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

    @Test
    public void testLongIntIntByteByteString() {
        testLongIntIntByteByteString(999999, 0, 123, (byte)99, (byte)1, "app7");
    }

    private void testLongIntIntByteByteString(long longValue, int intValue1, int intValue2, byte byteValue1, byte byteValue2, String stringValue) {
        AnnotationTranscoder transcoder = new AnnotationTranscoder();
        TLongIntIntByteByteStringValue value = new TLongIntIntByteByteStringValue();
        value.setLongValue(longValue);
        value.setIntValue1(intValue1);
        value.setIntValue2(intValue2);
        value.setByteValue1(byteValue1);
        value.setByteValue2(byteValue2);
        value.setStringValue(stringValue);
        byte[] encode = transcoder.encode(value, AnnotationTranscoder.CODE_LONG_INT_INT_BYTE_BYTE_STRING);
        LongIntIntByteByteStringValue decode = (LongIntIntByteByteStringValue) transcoder.decode(AnnotationTranscoder.CODE_LONG_INT_INT_BYTE_BYTE_STRING, encode);
        Assert.assertEquals(value.getLongValue(), decode.getLongValue());
        Assert.assertEquals(value.getIntValue1(), decode.getIntValue1());
        Assert.assertEquals(value.getIntValue2(), decode.getIntValue2());
        Assert.assertEquals(value.getByteValue1(), decode.getByteValue1());
        Assert.assertEquals(value.getByteValue2(), decode.getByteValue2());
        Assert.assertEquals(value.getStringValue(), decode.getStringValue());
    }

    @Test
    public void testIntBooleanIntBoolean() {
        AnnotationTranscoder transcoder = new AnnotationTranscoder();
        TIntBooleanIntBooleanValue value = new TIntBooleanIntBooleanValue();
        value.setIntValue1(10);
        value.setBoolValue1(false);
        value.setIntValue2(5000);
        value.setBoolValue2(true);

        byte[] encode = transcoder.encode(value, AnnotationTranscoder.CODE_INT_BOOLEAN_INT_BOOLEAN);
        IntBooleanIntBooleanValue decode = (IntBooleanIntBooleanValue) transcoder.decode(AnnotationTranscoder.CODE_INT_BOOLEAN_INT_BOOLEAN, encode);
        Assert.assertEquals(value.getIntValue1(), decode.getIntValue1());
        Assert.assertEquals(false, decode.isBooleanValue1());
        Assert.assertEquals(value.getIntValue2(), decode.getIntValue2());
        Assert.assertEquals(true, decode.isBooleanValue2());
    }

    private void write(int value) throws TException {
        TCompactProtocol.Factory factory = new TCompactProtocol.Factory();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
        TIOStreamTransport transport = new TIOStreamTransport(baos);
        TProtocol protocol = factory.getProtocol(transport);

        protocol.writeI32(value);
        byte[] buffer = baos.toByteArray();
        logger.debug(Arrays.toString(buffer));
    }

    @Test
    public void testEncode() throws Exception {

    }
}
