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

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.bo.RandomTSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactoryAssert;
import com.navercorp.pinpoint.common.server.bo.filter.EmptySpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanFactoryTest {

    private static final int REPEAT_COUNT = 10;

    private final long spanAcceptTime = System.currentTimeMillis();

    private final SpanFactory spanFactory = new SpanFactory();
    private final SpanEventFilter filter = new EmptySpanEventFilter();

    private final SpanFactoryAssert spanFactoryAssert = new SpanFactoryAssert();

    private final RandomTSpan random = new RandomTSpan();


    @Test
    public void testNewSpanBo() {
        TSpan tSpan = random.randomTSpan();

        SpanBo spanBo = spanFactory.newSpanBo(tSpan);

        spanFactoryAssert.assertSpan(tSpan, spanBo);
    }


    @Test
    public void testNewSpanBo_N() {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testNewSpanBo();
        }
    }


    @Test
    public void testNewSpanChunkBo() {
        TSpanChunk tSpanChunk = random.randomTSpanChunk();

        SpanChunkBo spanChunkBo = spanFactory.newSpanChunkBo(tSpanChunk);

        spanFactoryAssert.assertSpanChunk(tSpanChunk, spanChunkBo);

    }

    @Test
    public void testNewSpanChunkBo_N() {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testNewSpanChunkBo();
        }
    }

    @Test
    public void testNewSpanEventBo() {

        TSpanEvent tSpanEvent = random.randomTSpanEvent((short) RandomUtils.nextInt(0, 100));
        SpanEventBo spanEventBo = spanFactory.buildSpanEventBo(tSpanEvent);

        spanFactoryAssert.assertSpanEvent(tSpanEvent, spanEventBo);

    }

    @Test
    public void testNewSpanEventBo_N() {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testNewSpanEventBo();
        }
    }

    @Test
    public void testBuildSpanBo() {
        TSpan tSpan = random.randomTSpan();
        TSpanEvent tSpanEvent1 = random.randomTSpanEvent((short) 0);
        TSpanEvent tSpanEvent2 = random.randomTSpanEvent((short) 1);
        TSpanEvent tSpanEvent3 = random.randomTSpanEvent((short) 5);
        TSpanEvent tSpanEvent4 = random.randomTSpanEvent((short) 2);
        tSpan.setSpanEventList(List.of(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));

        SpanBo spanBo = spanFactory.buildSpanBo(tSpan, spanAcceptTime, filter);

        spanFactoryAssert.assertSpan(tSpan, spanBo);

    }

    @Test
    public void testBuildSpanBo_N() {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testBuildSpanBo();
        }
    }


    @Test
    public void testBuildSpanChunkBo() {
        TSpanChunk tSpanChunk = random.randomTSpanChunk();
        TSpanEvent tSpanEvent1 = random.randomTSpanEvent((short) 0);
        TSpanEvent tSpanEvent2 = random.randomTSpanEvent((short) 1);
        TSpanEvent tSpanEvent3 = random.randomTSpanEvent((short) 5);
        TSpanEvent tSpanEvent4 = random.randomTSpanEvent((short) 2);
        tSpanChunk.setSpanEventList(List.of(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));

        SpanChunkBo spanChunkBo = spanFactory.buildSpanChunkBo(tSpanChunk, spanAcceptTime, filter);

        spanFactoryAssert.assertSpanChunk(tSpanChunk, spanChunkBo);

    }

    @Test
    public void testBuildSpanChunkBo_N() {
        for (int i = 0; i < REPEAT_COUNT; i++) {
            testBuildSpanChunkBo();
        }
    }

    @Test
    public void testTransactionId_skip_agentId() {
        TSpan tSpan = new TSpan();
        tSpan.setAgentId("agentId");
        byte[] transactionIdBytes = TransactionIdUtils.formatBytes(null, 1, 2);
        tSpan.setTransactionId(transactionIdBytes);

        SpanBo spanBo = spanFactory.newSpanBo(tSpan);
        TransactionId transactionId = spanBo.getTransactionId();

        Assertions.assertEquals(transactionId.getAgentId().value(), "agentId");
        Assertions.assertEquals(transactionId.getAgentStartTime(), 1);
        Assertions.assertEquals(transactionId.getTransactionSequence(), 2);
    }

    @Test
    public void testTransactionId_include_agentId() {
        TSpan tSpan = new TSpan();
        tSpan.setAgentId("agentId");
        AgentId transactionAgentId = AgentId.of("transactionAgentId");
        byte[] transactionIdBytes = TransactionIdUtils.formatBytes(transactionAgentId, 1, 2);
        tSpan.setTransactionId(transactionIdBytes);

        SpanBo spanBo = spanFactory.newSpanBo(tSpan);
        TransactionId transactionId = spanBo.getTransactionId();

        Assertions.assertEquals(transactionId.getAgentId(), transactionAgentId);
        Assertions.assertEquals(transactionId.getAgentStartTime(), 1);
        Assertions.assertEquals(transactionId.getTransactionSequence(), 2);
    }


}