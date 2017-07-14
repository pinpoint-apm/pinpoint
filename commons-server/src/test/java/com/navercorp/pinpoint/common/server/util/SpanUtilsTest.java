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

package com.navercorp.pinpoint.common.server.util;

import java.util.Arrays;

import com.google.common.primitives.Longs;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.TraceRowKeyDecoderV2;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.thrift.dto.TSpan;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class SpanUtilsTest {
    @Test
    public void testGetTraceIndexRowKeyWhiteSpace() throws Exception {
        String agentId = "test test";
        long time = System.currentTimeMillis();
        check(agentId, time);
    }

    @Test
    public void testGetTraceIndexRowKey1() throws Exception {
        String agentId = "test";
        long time = System.currentTimeMillis();
        check(agentId, time);
    }

    @Test
    public void testGetTraceIndexRowKey2() throws Exception {
        String agentId = "";
        for (int i = 0; i < PinpointConstants.AGENT_NAME_MAX_LEN; i++) {
            agentId += "1";
        }

        long time = System.currentTimeMillis();
        check(agentId, time);
    }

    @Test
    public void testGetTraceIndexRowKey3() throws Exception {
        String agentId = "";
        for (int i = 0; i < PinpointConstants.AGENT_NAME_MAX_LEN + 1; i++) {
            agentId += "1";
        }

        long time = System.currentTimeMillis();
        try {
            check(agentId, time);
            Assert.fail("error");
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    private void check(String agentId0, long l1) {
        TSpan span = new TSpan();
        span.setAgentId(agentId0);
        span.setStartTime(l1);

        byte[] traceIndexRowKey = SpanUtils.getAgentIdTraceIndexRowKey(span.getAgentId(), span.getStartTime());

        String agentId = BytesUtils.toString(traceIndexRowKey, 0, PinpointConstants.AGENT_NAME_MAX_LEN).trim();
        Assert.assertEquals(agentId0, agentId);

        long time = Longs.fromByteArray(Arrays.copyOfRange(traceIndexRowKey, PinpointConstants.AGENT_NAME_MAX_LEN, PinpointConstants.AGENT_NAME_MAX_LEN + 8));
        time = TimeUtils.recoveryTimeMillis(time);
        Assert.assertEquals(time, l1);
    }

    @Test
    public void testGetTransactionId_BasicSpan() {
        SpanBo spanBo = new SpanBo();
        TransactionId spanTransactionId = new TransactionId("traceAgentId", System.currentTimeMillis(), 1111);
        spanBo.setTransactionId(spanTransactionId);

        byte[] transactionIdRowkey = SpanUtils.getTransactionId(spanBo);

        TraceRowKeyDecoderV2 decoder = new TraceRowKeyDecoderV2();
        TransactionId transactionId = decoder.readTransactionId(transactionIdRowkey);

        Assert.assertEquals(transactionId, spanBo.getTransactionId());
    }
}
