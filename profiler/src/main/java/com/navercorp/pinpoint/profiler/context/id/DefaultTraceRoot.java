/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTraceRoot implements TraceRoot {

    private final TraceId traceId;
    private final String agentId;
    private final long localTransactionId;

    private ByteBuffer compactTransactionId;
    private final long traceStartTime;

    private final Shared shared = new DefaultShared();


    public DefaultTraceRoot(TraceId traceId, String agentId, long traceStartTime, long localTransactionId) {
        if (traceId == null) {
            throw new NullPointerException("traceId must not be null");
        }
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }


        this.traceId = traceId;
        this.agentId = agentId;
        this.traceStartTime = traceStartTime;
        this.localTransactionId = localTransactionId;
    }

    @Override
    public TraceId getTraceId() {
        return traceId;
    }

    @Override
    public long getLocalTransactionId() {
        return localTransactionId;
    }


    @Override
    public long getTraceStartTime() {
        return traceStartTime;
    }

//    @Override
//    public void markTraceStartTime() {
//        this.traceStartTime = System.currentTimeMillis();
//    }


//    @Override
//    public long getTraceStartTime() {
//        final long traceStartTime = this.traceStartTime;
//        if (traceStartTime == -1) {
//            throw new IllegalStateException("traceStartTime not marked");
//        }
//        return traceStartTime;
//    }


    @Override
    public ByteBuffer getCompactTransactionId() {
        return asReadOnly(getCompactTransactionId0());
    }

    private ByteBuffer getCompactTransactionId0() {
        if (compactTransactionId != null) {
            return compactTransactionId;
        }
        this.compactTransactionId = encodeCompactTransactionId();
        return compactTransactionId;
    }

    private ByteBuffer encodeCompactTransactionId() {
        if (isCompactType()) {
            final String skipAgentId = null;
            return formatTransactionId(skipAgentId, this.traceId.getAgentStartTime(), traceId.getTransactionSequence());
        } else {
            return getBinaryTransactionId();
        }
    }

    private ByteBuffer asReadOnly(ByteBuffer byteBuffer) {
        return byteBuffer.asReadOnlyBuffer();
    }

    private boolean isCompactType() {
        return agentId.equals(traceId.getAgentId());
    }

    @Override
    public ByteBuffer getBinaryTransactionId() {
        final TraceId traceId = this.traceId;
        return TransactionIdUtils.formatByteBuffer(traceId.getAgentId(), traceId.getAgentStartTime(), traceId.getTransactionSequence());
    }


    private ByteBuffer formatTransactionId(String agentId, long agentStartTime, long transactionSequence) {
        return TransactionIdUtils.formatByteBuffer(agentId, agentStartTime, transactionSequence);
    }

    @Override
    public Shared getShared() {
        return shared;
    }




    @Override
    public String toString() {
        return "DefaultTraceRoot{" +
                "traceId=" + traceId +
                ", agentId='" + agentId + '\'' +
                ", traceStartTime=" + traceStartTime +
                '}';
    }
}
