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

package com.navercorp.pinpoint.common.server.bo.thrift;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.RandomTSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactoryAssert;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanFactoryTest {

    private static final int REPEAT_COUNT = 10;

    private final SpanFactory spanFactory = new SpanFactory();

    private SpanFactoryAssert spanFactoryAssert = new SpanFactoryAssert();

    private RandomTSpan random = new RandomTSpan();


    @Test
    public void testNewSpanBo() throws Exception {
        TSpan tSpan = random.randomTSpan();

        SpanBo spanBo = spanFactory.newSpanBo(tSpan);

        spanFactoryAssert.assertSpan(tSpan, spanBo);
    }


    @Test
    public void testNewSpanBo_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testNewSpanBo();
        }
    }


    @Test
    public void testNewSpanChunkBo() throws Exception {
        TSpanChunk tSpanChunk = random.randomTSpanChunk();

        SpanChunkBo spanChunkBo = spanFactory.newSpanChunkBo(tSpanChunk);

        spanFactoryAssert.assertSpanChunk(tSpanChunk, spanChunkBo);

    }

    @Test
    public void testNewSpanChunkBo_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testNewSpanChunkBo();
        }
    }

    @Test
    public void testNewSpanEventBo() throws Exception {

        TSpanEvent tSpanEvent = random.randomTSpanEvent((short) RandomUtils.nextInt(0, 100));
        SpanEventBo spanEventBo = spanFactory.buildSpanEventBo(tSpanEvent);

        spanFactoryAssert.assertSpanEvent(tSpanEvent, spanEventBo);

    }

    @Test
    public void testNewSpanEventBo_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testNewSpanEventBo();
        }
    }

    @Test
    public void testBuildSpanBo() throws Exception {
        TSpan tSpan = random.randomTSpan();
        TSpanEvent tSpanEvent1 = random.randomTSpanEvent((short)0);
        TSpanEvent tSpanEvent2 = random.randomTSpanEvent((short)1);
        TSpanEvent tSpanEvent3 = random.randomTSpanEvent((short)5);
        TSpanEvent tSpanEvent4 = random.randomTSpanEvent((short)2);
        tSpan.setSpanEventList(Lists.newArrayList(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));

        SpanBo spanBo = spanFactory.buildSpanBo(tSpan);

        spanFactoryAssert.assertSpan(tSpan, spanBo);

    }

    @Test
    public void testBuildSpanBo_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testBuildSpanBo();
        }
    }


    @Test
    public void testBuildSpanChunkBo() throws Exception {
        TSpanChunk tSpanChunk = random.randomTSpanChunk();
        TSpanEvent tSpanEvent1 = random.randomTSpanEvent((short)0);
        TSpanEvent tSpanEvent2 = random.randomTSpanEvent((short)1);
        TSpanEvent tSpanEvent3 = random.randomTSpanEvent((short)5);
        TSpanEvent tSpanEvent4 = random.randomTSpanEvent((short)2);
        tSpanChunk.setSpanEventList(Lists.newArrayList(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));

        SpanChunkBo spanChunkBo = spanFactory.buildSpanChunkBo(tSpanChunk);

        spanFactoryAssert.assertSpanChunk(tSpanChunk, spanChunkBo);

    }

    @Test
    public void testBuildSpanChunkBo_N() throws Exception {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testBuildSpanChunkBo();
        }
    }

    @Test
    public void testTransactionId_skip_agentId() throws Exception {
        TSpan tSpan = new TSpan();
        tSpan.setAgentId("agentId");
        byte[] transactionIdBytes = TransactionIdUtils.formatBytes(null, 1, 2);
        tSpan.setTransactionId(transactionIdBytes);

        SpanBo spanBo = spanFactory.newSpanBo(tSpan);
        TransactionId transactionId = spanBo.getTransactionId();

        Assert.assertEquals(transactionId.getAgentId(), "agentId");
        Assert.assertEquals(transactionId.getAgentStartTime(), 1);
        Assert.assertEquals(transactionId.getTransactionSequence(), 2);
    }

    @Test
    public void testTransactionId_include_agentId() throws Exception {
        TSpan tSpan = new TSpan();
        tSpan.setAgentId("agentId");
        byte[] transactionIdBytes = TransactionIdUtils.formatBytes("transactionAgentId", 1, 2);
        tSpan.setTransactionId(transactionIdBytes);

        SpanBo spanBo = spanFactory.newSpanBo(tSpan);
        TransactionId transactionId = spanBo.getTransactionId();

        Assert.assertEquals(transactionId.getAgentId(), "transactionAgentId");
        Assert.assertEquals(transactionId.getAgentStartTime(), 1);
        Assert.assertEquals(transactionId.getTransactionSequence(), 2);
    }


    @Test
    public void testFastLocalAsyncIdBo() throws Exception {
        int asyncId = 1;
        short asyncSequence = 0;
        TSpanEvent tSpanEvent = new TSpanEvent();

        tSpanEvent.setAsyncId(asyncId);
        tSpanEvent.setAsyncSequence(asyncSequence);
        LocalAsyncIdBo localAsyncIdBo = spanFactory.fastLocalAsyncIdBo(Collections.singletonList(tSpanEvent));

        Assert.assertEquals(localAsyncIdBo.getAsyncId(), asyncId);
        Assert.assertEquals(localAsyncIdBo.getSequence(), asyncSequence);
    }

    @Test
    public void testFastLocalAsyncIdBo_empty() throws Exception {

        TSpanEvent tSpanEvent = new TSpanEvent();

        LocalAsyncIdBo localAsyncIdBo = spanFactory.fastLocalAsyncIdBo(Collections.singletonList(tSpanEvent));
        Assert.assertNull(localAsyncIdBo);
    }

    @Test
    public void testFullScanLocalAsyncIdBo() throws Exception {
        int asyncId = 1;
        short asyncSequence = 0;
        TSpanEvent tSpanEvent = new TSpanEvent();
        tSpanEvent.setAsyncId(asyncId);
        tSpanEvent.setAsyncSequence(asyncSequence);

        TSpanChunk tSpanChunk = new TSpanChunk();
        tSpanChunk.setSpanEventList(Arrays.asList(tSpanEvent, tSpanEvent));

        LocalAsyncIdBo localAsyncIdBo = spanFactory.fullScanLocalAsyncIdBo(tSpanChunk);

        Assert.assertEquals(localAsyncIdBo.getAsyncId(), asyncId);
        Assert.assertEquals(localAsyncIdBo.getSequence(), asyncSequence);
    }

    @Test
    public void testFullScanLocalAsyncIdBo_empty() throws Exception {

        TSpanEvent tSpanEvent = new TSpanEvent();
        TSpanChunk tSpanChunk = new TSpanChunk();
        tSpanChunk.setSpanEventList(Arrays.asList(tSpanEvent, tSpanEvent));

        LocalAsyncIdBo localAsyncIdBo = spanFactory.fullScanLocalAsyncIdBo(tSpanChunk);
        Assert.assertNull(localAsyncIdBo);
    }

}