/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventBitFieldTest {
    @Test
    public void isSetHasException() throws Exception {

    }

    @Test
    public void setHasException_shortToByteCasting() throws Exception {
        SpanEventBitField field = new SpanEventBitField();
        field.setHasException(true);

        byte byteField = (byte) field.getBitField();

        SpanEventBitField byteCastField = new SpanEventBitField(byteField);
        Assert.assertTrue(byteCastField.isSetHasException());


    }

    @Test
    public void testRpc_first() throws Exception {
        SpanEventBo spanEventBo = new SpanEventBo();

        spanEventBo.setRpc("Rpc");

        SpanEventBitField bitField = SpanEventBitField.buildFirst(spanEventBo);
        Assert.assertFalse(bitField.isSetRpc());

    }

    @Test
    public void testEndPoint_first() throws Exception {
        SpanEventBo spanEventBo = new SpanEventBo();

        spanEventBo.setEndPoint("EndPoint");

        SpanEventBitField bitField = SpanEventBitField.buildFirst(spanEventBo);
        Assert.assertTrue(bitField.isSetEndPoint());

        bitField.setEndPoint(false);
        Assert.assertFalse(bitField.isSetEndPoint());

    }

    @Test
    public void testDestinationId_first() throws Exception {
        SpanEventBo spanEventBo = new SpanEventBo();

        spanEventBo.setDestinationId("DestinationId");

        SpanEventBitField bitField = SpanEventBitField.buildFirst(spanEventBo);
        Assert.assertTrue(bitField.isSetDestinationId());

        bitField.setDestinationId(false);
        Assert.assertFalse(bitField.isSetDestinationId());

    }


    @Test
    public void testNextSpanId_first() throws Exception {
        SpanEventBo spanEventBo = new SpanEventBo();

        spanEventBo.setNextSpanId(1234);

        SpanEventBitField bitField = SpanEventBitField.buildFirst(spanEventBo);
        Assert.assertTrue(bitField.isSetNextSpanId());

        bitField.setNextSpanId(false);
        Assert.assertFalse(bitField.isSetNextSpanId());

    }

    @Test
    public void testHasException_first() throws Exception {
        SpanEventBo spanEventBo = new SpanEventBo();

        spanEventBo.setExceptionInfo(100, "excetpion");

        SpanEventBitField bitField = SpanEventBitField.buildFirst(spanEventBo);
        Assert.assertTrue(bitField.isSetHasException());

        bitField.setHasException(false);
        Assert.assertFalse(bitField.isSetHasException());

    }


    @Test
    public void testAnnotation_first() throws Exception {
        SpanEventBo spanEventBo = new SpanEventBo();

        spanEventBo.setAnnotationBoList(Collections.singletonList(new AnnotationBo(1, "test")));

        SpanEventBitField bitField = SpanEventBitField.buildFirst(spanEventBo);
        Assert.assertTrue(bitField.isSetAnnotation());

        bitField.setAnnotation(false);
        Assert.assertFalse(bitField.isSetAnnotation());

    }


    @Test
    public void testNextAsyncId_first() throws Exception {
        SpanEventBo spanEventBo = new SpanEventBo();

        spanEventBo.setNextAsyncId(1234);

        SpanEventBitField bitField = SpanEventBitField.buildFirst(spanEventBo);
        Assert.assertTrue(bitField.isSetNextAsyncId());

        bitField.setNextAsyncId(false);
        Assert.assertFalse(bitField.isSetNextAsyncId());

    }

    @Test
    public void testAsyncId_first() throws Exception {
        SpanEventBo spanEventBo = new SpanEventBo();

        spanEventBo.setAsyncId(1234);
        spanEventBo.setAsyncSequence((short) 1234);

        SpanEventBitField bitField = SpanEventBitField.buildFirst(spanEventBo);
        Assert.assertTrue(bitField.isSetAsyncId());

        bitField.setAsyncId(false);
        Assert.assertFalse(bitField.isSetAsyncId());

    }

    @Test
    public void testStartElapsed_equals_next() throws Exception {
        SpanEventBo prev = new SpanEventBo();
        SpanEventBo current = new SpanEventBo();

        prev.setStartElapsed(1234);
        current.setStartElapsed(1234);

        SpanEventBitField bitField = SpanEventBitField.build(current, prev);
        Assert.assertEquals(bitField.getStartElapsedEncodingStrategy(), StartElapsedTimeEncodingStrategy.PREV_EQUALS);

    }

    @Test
    public void testStartElapsed_delta_next() throws Exception {
        SpanEventBo prev = new SpanEventBo();
        SpanEventBo current = new SpanEventBo();

        prev.setStartElapsed(1234);
        current.setStartElapsed(1235);

        SpanEventBitField bitField = SpanEventBitField.build(current, prev);
        Assert.assertEquals(bitField.getStartElapsedEncodingStrategy(), StartElapsedTimeEncodingStrategy.PREV_DELTA);

    }

    @Test
    public void testSequence_add1_next() throws Exception {
        SpanEventBo prev = new SpanEventBo();
        SpanEventBo current = new SpanEventBo();

        prev.setSequence((short) 10);
        current.setSequence((short) 11);

        SpanEventBitField bitField = SpanEventBitField.build(current, prev);
        Assert.assertEquals(bitField.getSequenceEncodingStrategy(), SequenceEncodingStrategy.PREV_ADD1);

    }

    @Test
    public void testSequence_delta_next() throws Exception {
        SpanEventBo prev = new SpanEventBo();
        SpanEventBo current = new SpanEventBo();

        prev.setSequence((short) 10);
        current.setSequence((short) 12);

        SpanEventBitField bitField = SpanEventBitField.build(current, prev);
        Assert.assertEquals(bitField.getSequenceEncodingStrategy(), SequenceEncodingStrategy.PREV_DELTA);

    }

    @Test
    public void testDepth_equals_next() throws Exception {
        SpanEventBo prev = new SpanEventBo();
        SpanEventBo current = new SpanEventBo();

        prev.setDepth(3);
        current.setDepth(3);

        SpanEventBitField bitField = SpanEventBitField.build(current, prev);
        Assert.assertEquals(bitField.getDepthEncodingStrategy(), DepthEncodingStrategy.PREV_EQUALS);

    }

    @Test
    public void testDepth_raw_next() throws Exception {
        SpanEventBo prev = new SpanEventBo();
        SpanEventBo current = new SpanEventBo();

        prev.setDepth(3);
        current.setDepth(4);

        SpanEventBitField bitField = SpanEventBitField.build(current, prev);
        Assert.assertEquals(bitField.getDepthEncodingStrategy(), DepthEncodingStrategy.RAW);

    }

    @Test
    public void testServiceType_equals_next() throws Exception {
        SpanEventBo prev = new SpanEventBo();
        SpanEventBo current = new SpanEventBo();

        prev.setServiceType((short) 2000);
        current.setServiceType((short) 2000);

        SpanEventBitField bitField = SpanEventBitField.build(current, prev);
        Assert.assertEquals(bitField.getServiceTypeEncodingStrategy(), ServiceTypeEncodingStrategy.PREV_EQUALS);

    }

    @Test
    public void testServiceType_raw_next() throws Exception {
        SpanEventBo prev = new SpanEventBo();
        SpanEventBo current = new SpanEventBo();

        prev.setServiceType((short) 2000);
        current.setServiceType((short) 2001);

        SpanEventBitField bitField = SpanEventBitField.build(current, prev);
        Assert.assertEquals(bitField.getServiceTypeEncodingStrategy(), ServiceTypeEncodingStrategy.RAW);

    }


}