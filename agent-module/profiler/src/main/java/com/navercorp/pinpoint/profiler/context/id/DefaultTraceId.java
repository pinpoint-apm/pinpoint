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

package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;

import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultTraceId implements TraceId {

    private final AgentId agentId;
    private final long agentStartTime;
    private final long transactionSequence;

    private final long parentSpanId;
    private final long spanId;
    private final short flags;

    public DefaultTraceId(AgentId agentId, long agentStartTime, long transactionId) {
        this(agentId, agentStartTime, transactionId, SpanId.NULL, SpanId.newSpanId(), (short) 0);
    }

    @Override
    public TraceId getNextTraceId() {
        return new DefaultTraceId(this.agentId, this.agentStartTime, transactionSequence, spanId, SpanId.nextSpanID(spanId, parentSpanId), flags);
    }

    public DefaultTraceId(AgentId agentId, long agentStartTime, long transactionId, long parentSpanId, long spanId, short flags) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
        this.transactionSequence = transactionId;

        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
        this.flags = flags;
    }

    @Override
    public String getTransactionId() {
        return TransactionIdUtils.formatString(agentId, agentStartTime, transactionSequence);
    }

    @Override
    public AgentId getAgentId() {
        return agentId;
    }

    @Override
    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public long getTransactionSequence() {
        return transactionSequence;
    }


    @Override
    public long getParentSpanId() {
        return parentSpanId;
    }

    @Override
    public long getSpanId() {
        return spanId;
    }


    @Override
    public short getFlags() {
        return flags;
    }

    @Override
    public boolean isRoot() {
        return this.parentSpanId == SpanId.NULL;
    }

    @Override
    public String toString() {
        return "DefaultTraceId{" + "agentId='" + agentId + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", transactionSequence=" + transactionSequence +
                ", parentSpanId=" + parentSpanId +
                ", spanId=" + spanId +
                ", flags=" + flags +
                '}';
    }

}
