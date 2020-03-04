/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ContinueTraceHeader implements TraceHeader {
    private final String transactionId;
    private long parentSpanId;
    private long spanId;
    private short flags;

    public ContinueTraceHeader(String transactionId, long parentSpanId, long spanId, short flags) {
        this.transactionId = Assert.requireNonNull(transactionId, "transactionId");
        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
        this.flags = flags;
    }


    @Override
    public TraceHeaderState getState() {
        return TraceHeaderState.CONTINUE;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
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
    public String toString() {
        return "ContinueTraceHeader{" +
                "transactionId='" + transactionId + '\'' +
                ", parentSpanId=" + parentSpanId +
                ", spanId=" + spanId +
                ", flags=" + flags +
                '}';
    }
}
