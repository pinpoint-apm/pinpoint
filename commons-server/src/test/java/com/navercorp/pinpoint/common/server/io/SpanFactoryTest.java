/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.io;

import com.navercorp.pinpoint.common.server.bo.RandomTSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactoryAssert;
import com.navercorp.pinpoint.common.server.bo.filter.EmptySpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import com.navercorp.pinpoint.io.SpanVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanFactoryTest {

    private static final int REPEAT_COUNT = 10;

    private final long spanAcceptTime = System.currentTimeMillis();

    private final ServerHeader header = new DefaultServerHeader("agentId", "agentName", "applicationName", "serviceName", () -> ServiceUid.DEFAULT, 88, 1000, false);

    private final GrpcSpanBinder grpcSpanBinder = new GrpcSpanBinder();
    private final SpanEventFilter filter = new EmptySpanEventFilter();
    private final GrpcSpanFactory grpcSpanFactory = new CollectorGrpcSpanFactory(grpcSpanBinder, filter);

    private final SpanFactoryAssert spanFactoryAssert = new SpanFactoryAssert();

    private final RandomTSpan random = new RandomTSpan();
    private final Random RANDOM = new Random();

    @RepeatedTest(REPEAT_COUNT)
    public void testNewSpanBo() {
        PSpan.Builder pSpan = random.randomPSpan();

        SpanBo spanBo = grpcSpanBinder.newSpanBo(pSpan.build(), header, spanAcceptTime);

        spanFactoryAssert.assertSpan(pSpan.build(), spanBo);
    }


    @RepeatedTest(REPEAT_COUNT)
    public void testNewSpanChunkBo() {
        PSpanChunk.Builder tSpanChunk = random.randomTSpanChunk();

        SpanChunkBo spanChunkBo = grpcSpanBinder.bindSpanChunkBo(tSpanChunk.build(), header, spanAcceptTime);

        spanFactoryAssert.assertSpanChunk(tSpanChunk.build(), spanChunkBo);

    }

    @RepeatedTest(REPEAT_COUNT)
    public void testNewSpanEventBo() {

        PSpanEvent tSpanEvent = random.randomTSpanEvent((short) RANDOM.nextInt(0, 100));
        SpanEventBo spanEventBo = grpcSpanBinder.bindSpanEventBoList(List.of(tSpanEvent)).get(0);

        spanFactoryAssert.assertSpanEvent(tSpanEvent, 0, spanEventBo);

    }


    @RepeatedTest(REPEAT_COUNT)
    public void testBuildSpanBo() {
        PSpan.Builder span = random.randomPSpan();
        PSpanEvent tSpanEvent1 = random.randomTSpanEvent((short) 0);
        PSpanEvent tSpanEvent2 = random.randomTSpanEvent((short) 1);
        PSpanEvent tSpanEvent3 = random.randomTSpanEvent((short) 5);
        PSpanEvent tSpanEvent4 = random.randomTSpanEvent((short) 2);
        span.addAllSpanEvent(List.of(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));

        SpanBo spanBo = grpcSpanFactory.buildSpanBo(span.build(), header, spanAcceptTime);

        spanFactoryAssert.assertSpan(span.build(), spanBo);
    }


    @RepeatedTest(REPEAT_COUNT)
    public void testBuildSpanChunkBo() {
        PSpanChunk.Builder tSpanChunk = random.randomTSpanChunk();
        PSpanEvent tSpanEvent1 = random.randomTSpanEvent((short) 0);
        PSpanEvent tSpanEvent2 = random.randomTSpanEvent((short) 1);
        PSpanEvent tSpanEvent3 = random.randomTSpanEvent((short) 5);
        PSpanEvent tSpanEvent4 = random.randomTSpanEvent((short) 2);
        tSpanChunk.addAllSpanEvent(List.of(tSpanEvent1, tSpanEvent2, tSpanEvent3, tSpanEvent4));

        SpanChunkBo spanChunkBo = grpcSpanFactory.buildSpanChunkBo(tSpanChunk.build(), header, spanAcceptTime);

        spanFactoryAssert.assertSpanChunk(tSpanChunk.build(), spanChunkBo);

    }

    @Test
    public void testTransactionId_skip_agentId() {
        PSpan.Builder tSpan = PSpan.newBuilder();
        PTransactionId pTransactionId = PTransactionId.newBuilder()
                .setAgentStartTime(1)
                .setSequence(2)
                .build();
        tSpan.setTransactionId(pTransactionId);
        tSpan.setVersion(SpanVersion.TRACE_V2);

        SpanBo spanBo = grpcSpanBinder.bindSpanBo(tSpan.build(), header, spanAcceptTime);
        ServerTraceId transactionId = spanBo.getTransactionId();

        PinpointServerTraceId pinpointServerTraceId = (PinpointServerTraceId) transactionId;
        Assertions.assertEquals("agentId", pinpointServerTraceId.getAgentId());
        Assertions.assertEquals(1, pinpointServerTraceId.getAgentStartTime());
        Assertions.assertEquals(2, pinpointServerTraceId.getTransactionSequence());
    }

    @Test
    public void testTransactionId_include_agentId() {
        PTransactionId pTransaction = PTransactionId.newBuilder()
                .setAgentId("transactionAgentId")
                .setAgentStartTime(1)
                .setSequence(2)
                .build();
        PSpan tSpan = PSpan.newBuilder()
                .setTransactionId(pTransaction)
                .setVersion(SpanVersion.TRACE_V2)
                .build();

        SpanBo spanBo = grpcSpanFactory.buildSpanBo(tSpan, header, spanAcceptTime);
        ServerTraceId transactionId = spanBo.getTransactionId();

        PinpointServerTraceId pinpointServerTraceId = (PinpointServerTraceId) transactionId;
        Assertions.assertEquals("transactionAgentId", pinpointServerTraceId.getAgentId());
        Assertions.assertEquals(1, pinpointServerTraceId.getAgentStartTime());
        Assertions.assertEquals(2, pinpointServerTraceId.getTransactionSequence());
    }


}