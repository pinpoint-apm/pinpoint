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

import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;

/**
 * @author emeroad
 */
public class DefaultTraceId implements TraceId {

    private final String agentId;
    private final long agentStartTime;
    private final long transactionSequence;

    private final long parentSpanId;
    private final long spanId;
    private final short flags;

    public DefaultTraceId(String agentId, long agentStartTime, long transactionId) {
        this(agentId, agentStartTime, transactionId, SpanId.NULL, SpanId.newSpanId(), (short) 0);
    }

    public TraceId getNextTraceId() {
        return new DefaultTraceId(this.agentId, this.agentStartTime, transactionSequence, spanId, SpanId.nextSpanID(spanId, parentSpanId), flags);
    }

    public DefaultTraceId(String agentId, long agentStartTime, long transactionId, long parentSpanId, long spanId, short flags) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.transactionSequence = transactionId;

        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
        this.flags = flags;
    }

    public String getTransactionId() {
        return TransactionIdUtils.formatString(agentId, agentStartTime, transactionSequence);
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public long getTransactionSequence() {
        return transactionSequence;
    }


    public long getParentSpanId() {
        return parentSpanId;
    }

    public long getSpanId() {
        return spanId;
    }


    public short getFlags() {
        return flags;
    }

    public boolean isRoot() {
        return this.parentSpanId == SpanId.NULL;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultTraceId{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", agentStartTime=").append(agentStartTime);
        sb.append(", transactionSequence=").append(transactionSequence);
        sb.append(", parentSpanId=").append(parentSpanId);
        sb.append(", spanId=").append(spanId);
        sb.append(", flags=").append(flags);
        sb.append('}');
        return sb.toString();
    }

}
