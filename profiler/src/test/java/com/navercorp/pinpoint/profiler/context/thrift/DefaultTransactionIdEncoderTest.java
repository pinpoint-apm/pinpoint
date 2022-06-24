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

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTransactionIdEncoderTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final String DEFAULT_AGENT_ID = "defaultAgentId";

    private final String agentId = "agentId";
    private final long agentStartTime = 11;
    private final long transactionId = 1;

    private final String agentId2 = "agentId2";
    private final long agentStartTime2 = 12;
    private final long transactionId2 = 2;

    private final DefaultTransactionIdEncoder transactionIdEncoder = new DefaultTransactionIdEncoder(agentId, agentStartTime);

    @Test
    public void testCompressedTransactionId() {
        TraceRoot traceRoot = getRootTraceId();
        TraceId traceId = traceRoot.getTraceId();

        ByteBuffer encodeTxId = transactionIdEncoder.encodeTransactionId(traceId);

        byte[] transactionId = Arrays.copyOf(encodeTxId.array(), encodeTxId.remaining());
        TransactionId parsedTxId = TransactionIdUtils.parseTransactionId(transactionId, DEFAULT_AGENT_ID);
        logger.debug("transactionId:{}", parsedTxId);
        Assertions.assertEquals(DEFAULT_AGENT_ID, parsedTxId.getAgentId());
        Assertions.assertEquals(parsedTxId.getAgentStartTime(), agentStartTime);
        Assertions.assertEquals(parsedTxId.getTransactionSequence(), transactionId2);
    }

    @Test
    public void testNormalTransactionId() {
        TraceRoot traceRoot = getExternalTraceId();

        TraceId traceId = traceRoot.getTraceId();
        ByteBuffer encodeTxId = transactionIdEncoder.encodeTransactionId(traceId);


        byte[] transactionId = Arrays.copyOf(encodeTxId.array(), encodeTxId.remaining());
        TransactionId parsedTxId = TransactionIdUtils.parseTransactionId(transactionId, DEFAULT_AGENT_ID);
        logger.debug("transactionId:{}", parsedTxId);
        Assertions.assertEquals(parsedTxId.getAgentId(), agentId2);
        Assertions.assertEquals(parsedTxId.getAgentStartTime(), agentStartTime2);
        Assertions.assertEquals(parsedTxId.getTransactionSequence(), transactionId2);
    }

    @Test
    public void testDuplicateAgentId() {
        TraceRoot traceRoot = getDuplicateAgentId();

        TraceId traceId = traceRoot.getTraceId();
        ByteBuffer encodeTxId = transactionIdEncoder.encodeTransactionId(traceId);


        byte[] transactionId = Arrays.copyOf(encodeTxId.array(), encodeTxId.remaining());
        TransactionId parsedTxId = TransactionIdUtils.parseTransactionId(transactionId, DEFAULT_AGENT_ID);
        logger.debug("transactionId:{}", parsedTxId);
        Assertions.assertEquals(DEFAULT_AGENT_ID, parsedTxId.getAgentId());
        Assertions.assertEquals(parsedTxId.getAgentStartTime(), agentStartTime2);
        Assertions.assertEquals(parsedTxId.getTransactionSequence(), transactionId2);
    }


    private TraceRoot getRootTraceId() {
        TraceId traceId = new DefaultTraceId(agentId, agentStartTime, transactionId2);
        return new DefaultTraceRoot(traceId, agentId, agentStartTime, transactionId);
    }

    private TraceRoot getExternalTraceId() {
        TraceId traceId = new DefaultTraceId(agentId2, agentStartTime2, transactionId2);
        return new DefaultTraceRoot(traceId, agentId, agentStartTime, transactionId);
    }

    private TraceRoot getDuplicateAgentId() {
        TraceId traceId = new DefaultTraceId(agentId, agentStartTime2, transactionId2);
        return new DefaultTraceRoot(traceId, agentId, agentStartTime, transactionId);
    }


}