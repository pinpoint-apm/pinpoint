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

package com.navercorp.pinpoint.common.bo;


import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author emeroad
 */
public class SpanBoTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testVersion() {
        SpanBo spanBo = new SpanBo();
        check(spanBo, 0);
        check(spanBo, 254);
        check(spanBo, 255);
        try {
            check(spanBo, 256);
            Assert.fail();
        } catch (Exception ignored) {
        }


    }

    private void check(SpanBo spanBo, int v) {
        spanBo.setVersion(v);
        int version = spanBo.getVersion();

        Assert.assertEquals(v, version);
    }

    @Test
    public void serialize() {
        SpanBo spanBo = new SpanBo();
        spanBo.setAgentId("agentId");
        spanBo.setApplicationId("applicationId");
        spanBo.setEndPoint("end");
        spanBo.setRpc("rpc");

        spanBo.setParentSpanId(5);

        spanBo.setAgentStartTime(1);
        spanBo.setTraceAgentStartTime(2);
        spanBo.setTraceTransactionSequence(3);
        spanBo.setElapsed(4);
        spanBo.setStartTime(5);


        spanBo.setServiceType(ServiceType.STAND_ALONE.getCode());
        byte[] bytes = spanBo.writeValue();
        logger.info("length:{}", bytes.length);

        SpanBo newSpanBo = new SpanBo();
        int i = newSpanBo.readValue(bytes, 0);
        logger.info("length:{}", i);
        Assert.assertEquals(bytes.length, i);
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


    }

    @Test
    public void serialize2() {
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

        byte[] bytes = spanBo.writeValue();
        logger.info("length:{}", bytes.length);

        SpanBo newSpanBo = new SpanBo();
        int i = newSpanBo.readValue(bytes, 0);
        logger.info("length:{}", i);
        Assert.assertEquals(bytes.length, i);
        
        Assert.assertEquals(spanBo.getServiceType(), spanBo.getServiceType());
        Assert.assertEquals(spanBo.getApplicationServiceType(), spanBo.getApplicationServiceType());
    }

    private String createString(int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append('a');
        }
        return sb.toString();
    }

}
