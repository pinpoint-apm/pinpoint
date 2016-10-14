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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author emeroad
 */
public class SpanEventBoTest {


    private SpanEventSerializer serializer = new SpanEventSerializer();
    private SpanDecoder spanDecoder = new SpanDecoder();

    @Before
    public void setUp() throws Exception {

        this.serializer = new SpanEventSerializer();
        final AnnotationSerializer annotationSerializer = new AnnotationSerializer();
        this.serializer.setAnnotationSerializer(annotationSerializer);

    }

    @Test
    public void testSerialize() throws Exception {
        SpanBo spanBo = new SpanBo();
        spanBo.setAgentId("testAgent");
        spanBo.setApplicationId("testApp");
        spanBo.setAgentStartTime(1);
        spanBo.setSpanId(12);

        SpanEventBo spanEventBo = new SpanEventBo();
        spanEventBo.setDepth(3);
        spanEventBo.setDestinationId("testdest");
        spanEventBo.setEndElapsed(2);
        spanEventBo.setEndPoint("endpoint");

        spanEventBo.setNextSpanId(4);
        spanEventBo.setRpc("rpc");

        spanEventBo.setServiceType(ServiceType.STAND_ALONE.getCode());
        spanEventBo.setStartElapsed(100);
        spanEventBo.setNextAsyncId(1000);

        SpanEventEncodingContext spanEventEncodingContext = new SpanEventEncodingContext(spanBo, spanEventBo);
        ByteBuffer bytes = serializer.writeValue(spanEventEncodingContext);

        SpanEventBo newSpanEventBo = new SpanEventBo();
        SpanDecodingContext spanDecodingContext = new SpanDecodingContext();
        Buffer buffer = new OffsetFixedBuffer(bytes.array(), bytes.arrayOffset(), bytes.remaining());
        int i = spanDecoder.readSpanEvent(newSpanEventBo, buffer, spanDecodingContext);
        Assert.assertEquals(bytes.limit(), i);


        Assert.assertEquals(spanBo.getAgentId(), spanDecodingContext.getAgentId());
        Assert.assertEquals(spanBo.getApplicationId(), spanDecodingContext.getApplicationId());
        Assert.assertEquals(spanBo.getAgentStartTime(), spanDecodingContext.getAgentStartTime());
        Assert.assertEquals(spanEventBo.getDepth(), newSpanEventBo.getDepth());
        Assert.assertEquals(spanEventBo.getDestinationId(), newSpanEventBo.getDestinationId());
        Assert.assertEquals(spanEventBo.getEndElapsed(), newSpanEventBo.getEndElapsed());
        Assert.assertEquals(spanEventBo.getEndPoint(), newSpanEventBo.getEndPoint());


        Assert.assertEquals(spanEventBo.getNextSpanId(), newSpanEventBo.getNextSpanId());
        Assert.assertEquals(spanEventBo.getRpc(), newSpanEventBo.getRpc());
        Assert.assertEquals(spanEventBo.getServiceType(), newSpanEventBo.getServiceType());
        Assert.assertEquals(spanEventBo.getStartElapsed(), newSpanEventBo.getStartElapsed());

        Assert.assertEquals(spanEventBo.getNextAsyncId(), newSpanEventBo.getNextAsyncId());

        spanEventBo.setSequence((short) 3);
        newSpanEventBo.setSequence((short) 3);
        Assert.assertEquals(spanEventBo.getSequence(), newSpanEventBo.getSequence());
    }
}
