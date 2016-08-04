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
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.util.TransactionId;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.trace.ServiceType;

import java.nio.ByteBuffer;

/**
 * @author emeroad
 */
public class SpanBoTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SpanSerializer spanSerializer = new SpanSerializer();
    private final SpanDecoder spanDecoder = new SpanDecoder();

    @Test
    public void testVersion() {
        SpanBo spanBo = new SpanBo();
        checkVersion(spanBo, 0);
        checkVersion(spanBo, 254);
        checkVersion(spanBo, 255);
        try {
            checkVersion(spanBo, 256);
            Assert.fail();
        } catch (Exception ignored) {
        }

        byte byteVersion = 2;
        spanBo.setVersion(byteVersion);
        Assert.assertTrue(spanBo.getRawVersion() == byteVersion);

    }

    private void checkVersion(SpanBo spanBo, int v) {
        spanBo.setVersion(v);
        int version = spanBo.getVersion();

        Assert.assertEquals(v, version);
    }

    @Test
    public void serialize_V1() {
        final SpanBo spanBo = new SpanBo();
        spanBo.setAgentId("agentId");
        spanBo.setApplicationId("applicationId");
        spanBo.setEndPoint("end");
        spanBo.setRpc("rpc");

        spanBo.setParentSpanId(5);
        spanBo.setAgentStartTime(1);

        TransactionId transactionId = new TransactionId("agentId", 2, 3);
        spanBo.setTransactionId(transactionId);
        spanBo.setElapsed(4);
        spanBo.setStartTime(5);


        spanBo.setServiceType(ServiceType.STAND_ALONE.getCode());

        spanBo.setLoggingTransactionInfo(LoggingInfo.INFO.getCode());

        spanBo.setExceptionInfo(1000, "Exception");

        ByteBuffer bytes = spanSerializer.writeColumnValue(spanBo);

        SpanBo newSpanBo = new SpanBo();
        Buffer valueBuffer = new OffsetFixedBuffer(bytes.array(), bytes.arrayOffset(), bytes.remaining());
        int i = spanDecoder.readSpan(newSpanBo, valueBuffer);
        logger.debug("length:{}", i);
        Assert.assertEquals(bytes.limit(), i);
        Assert.assertEquals(newSpanBo.getAgentId(), spanBo.getAgentId());
        Assert.assertEquals(newSpanBo.getApplicationId(), spanBo.getApplicationId());
        Assert.assertEquals(newSpanBo.getAgentStartTime(), spanBo.getAgentStartTime());
        Assert.assertEquals(newSpanBo.getElapsed(), spanBo.getElapsed());
        Assert.assertEquals(newSpanBo.getEndPoint(), spanBo.getEndPoint());
        Assert.assertEquals(newSpanBo.getErrCode(), spanBo.getErrCode());
        Assert.assertEquals(newSpanBo.getFlag(), spanBo.getFlag());

//        not included for serialization
//        Assert.assertEquals(newSpanBo.getTraceAgentStartTime(), spanBo.getTraceAgentStartTime());
//        Assert.assertEquals(newSpanBo.getTraceTransactionSequence(), spanBo.getTraceTransactionSequence());
        Assert.assertEquals(newSpanBo.getParentSpanId(), spanBo.getParentSpanId());
        
        Assert.assertEquals(newSpanBo.getServiceType(), spanBo.getServiceType());
        Assert.assertEquals(newSpanBo.getApplicationServiceType(), spanBo.getServiceType());

        Assert.assertEquals(newSpanBo.getVersion(), spanBo.getVersion());

        Assert.assertEquals(newSpanBo.getLoggingTransactionInfo(), spanBo.getLoggingTransactionInfo());


        Assert.assertEquals(newSpanBo.getExceptionId(), spanBo.getExceptionId());
        Assert.assertEquals(newSpanBo.getExceptionMessage(), spanBo.getExceptionMessage());


    }

    @Test
    public void serialize2_V1() {
        SpanBo spanBo = new SpanBo();
        spanBo.setAgentId("agent");
        String service = createString(5);
        spanBo.setApplicationId(service);
        String endPoint = createString(127);
        spanBo.setEndPoint(endPoint);
        String rpc = createString(255);
        spanBo.setRpc(rpc);

        spanBo.setServiceType(ServiceType.STAND_ALONE.getCode());
        spanBo.setApplicationServiceType(ServiceType.UNKNOWN.getCode());

        final ByteBuffer bytes = spanSerializer.writeColumnValue(spanBo);


        SpanBo newSpanBo = new SpanBo();
        Buffer valueBuffer = new OffsetFixedBuffer(bytes.array(), bytes.arrayOffset(), bytes.remaining());
        int i = spanDecoder.readSpan(newSpanBo, valueBuffer);
        logger.debug("length:{}", i);
        Assert.assertEquals(bytes.limit(), i);
        
        Assert.assertEquals(spanBo.getServiceType(), spanBo.getServiceType());
        Assert.assertEquals(spanBo.getApplicationServiceType(), spanBo.getApplicationServiceType());
    }

    private String createString(int size) {
        return RandomStringUtils.random(size);
    }

}
