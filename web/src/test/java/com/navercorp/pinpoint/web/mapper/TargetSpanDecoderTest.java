/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoder;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.SpanHint;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class TargetSpanDecoderTest {

    @Test(expected = NullPointerException.class)
    public void constructorFailureTest1() {
        new TargetSpanDecoder(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void constructorFailureTest2() {
        SpanDecoder mockSpanDecoder = Mockito.mock(SpanDecoder.class);
        new TargetSpanDecoder(mockSpanDecoder, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFailureTest3() {
        SpanDecoder mockSpanDecoder = Mockito.mock(SpanDecoder.class);

        TransactionId transactionId = Random.createTransactionId();
        GetTraceInfo getTraceInfo = new GetTraceInfo(transactionId);

        new TargetSpanDecoder(mockSpanDecoder, getTraceInfo);
    }

    @Test
    public void decodeTest1() {
        SpanDecoder mockSpanDecoder = createMockSpanDecoder();

        GetTraceInfo getTraceInfo = createGetTraceInfo();

        TargetSpanDecoder targetSpanDecoder = new TargetSpanDecoder(mockSpanDecoder, getTraceInfo);

        Object result = targetSpanDecoder.decode(null, null, null);
        Assert.assertNull(result);
    }

    @Test
    public void decodeTest2() {
        SpanBo spanBo = Random.createSpanBo();
        SpanDecoder mockSpanDecoder = createMockSpanDecoder(spanBo);

        GetTraceInfo getTraceInfo = createGetTraceInfo(spanBo);

        TargetSpanDecoder targetSpanDecoder = new TargetSpanDecoder(mockSpanDecoder, getTraceInfo);

        Object result = targetSpanDecoder.decode(null, null, null);
        Assert.assertNotNull(result);
    }

    private GetTraceInfo createGetTraceInfo() {
        return createGetTraceInfo(Random.createSpanBo());
    }

    private GetTraceInfo createGetTraceInfo(SpanBo spanBo) {
        return new GetTraceInfo(spanBo.getTransactionId(), new SpanHint(spanBo.getCollectorAcceptTime(), spanBo.getElapsed()));
    }

    private SpanDecoder createMockSpanDecoder() {
        return createMockSpanDecoder(Random.createSpanBo());
    }

    private SpanDecoder createMockSpanDecoder(SpanBo spanBo) {
        SpanDecoder mockSpanDecoder = Mockito.mock(SpanDecoder.class);
        Mockito.when(mockSpanDecoder.decode(null, null, null)).thenReturn(spanBo);
        return mockSpanDecoder;
    }


    private static class Random {

        private static SpanBo createSpanBo() {
            SpanBo spanBo = new SpanBo();
            spanBo.setTransactionId(createTransactionId());
            spanBo.setCollectorAcceptTime(createCollectorAcceptTime());
            spanBo.setElapsed(createElapsed());
            return spanBo;
        }

        private static TransactionId createTransactionId() {
            String agentId = RandomStringUtils.randomAlphanumeric(4, 24);

            long boundAgentStartTime = System.currentTimeMillis();
            long originAgentStartTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
            long agentStartTime = ThreadLocalRandom.current().nextLong(originAgentStartTime, boundAgentStartTime);

            int transactionSequence = ThreadLocalRandom.current().nextInt(0, 10);
            return new TransactionId(agentId, agentStartTime, transactionSequence);
        }

        private static long createCollectorAcceptTime() {
            long currentTimeMillis = System.currentTimeMillis();
            return ThreadLocalRandom.current().nextLong(currentTimeMillis - TimeUnit.DAYS.toMillis(30), currentTimeMillis);
        }

        private static int createElapsed() {
            return ThreadLocalRandom.current().nextInt((int) TimeUnit.SECONDS.toMillis(1), (int) TimeUnit.SECONDS.toMillis(10));
        }

    }

}
