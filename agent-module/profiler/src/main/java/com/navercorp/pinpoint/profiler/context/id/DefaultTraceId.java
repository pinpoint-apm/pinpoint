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
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionUId;

import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultTraceId implements TraceId {

    private final TransactionId transactionId;
    private final TransactionUId transactionUId;

    private final long parentSpanId;
    private final long spanId;
    private final short flags;


    public DefaultTraceId(TransactionId transactionId) {
        this(transactionId, null, SpanId.NULL, SpanId.newSpanId(), (short) 0);
    }

    public static TraceId v4(TransactionId transactionId, TransactionUId txId) {
        return new DefaultTraceId(transactionId, txId, SpanId.NULL, SpanId.newSpanId(), (short) 0);
    }

    public DefaultTraceId(TransactionId transactionId,
                          TransactionUId transactionUId,
                          long parentSpanId, long spanId, short flags) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");

        // optional
        this.transactionUId = transactionUId;

        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
        this.flags = flags;
    }

    public TraceId getNextTraceId() {
        return new DefaultTraceId(transactionId, transactionUId, spanId, SpanId.nextSpanID(spanId, parentSpanId), flags);
    }

    public String getTransactionId() {
        return transactionId.toString();
    }

    public TransactionId getInternalTransactionId() {
        return transactionId;
    }

    @Override
    public String getTransactionUId() {
        return transactionUId.toString();
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
        return "DefaultTraceId{" +
                "transactionId=" + transactionId +
                ", transactionUId=" + transactionUId +
                ", parentSpanId=" + parentSpanId +
                ", spanId=" + spanId +
                ", flags=" + flags +
                '}';
    }
}
