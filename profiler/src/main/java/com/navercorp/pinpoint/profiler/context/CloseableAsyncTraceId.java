/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceCloseable;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;

/**
 * @author jaehong.kim
 */
@InterfaceAudience.LimitedPrivate("vert.x")
public class CloseableAsyncTraceId implements AsyncTraceId, AsyncTraceCloseable {
    private final AsyncTraceId traceId;
    private final AsyncTraceCloseable closeable;

    public CloseableAsyncTraceId(final AsyncTraceId traceId, final AsyncTraceCloseable closeable) {
        if (traceId == null || closeable == null) {
            throw new IllegalArgumentException("traceId or closeable must not be null");
        }
        this.traceId = traceId;
        this.closeable = closeable;
    }

    @Override
    public int getAsyncId() {
        return this.traceId.getAsyncId();
    }

    @Override
    public short nextAsyncSequence() {
        return this.traceId.nextAsyncSequence();
    }

    @Override
    public TraceId getNextTraceId() {
        return traceId.getNextTraceId();
    }

    @Override
    public long getSpanId() {
        return traceId.getSpanId();
    }

    @Override
    public String getTransactionId() {
        return traceId.getTransactionId();
    }

    @Override
    public String getAgentId() {
        return traceId.getAgentId();
    }

    @Override
    public long getAgentStartTime() {
        return traceId.getAgentStartTime();
    }

    @Override
    public long getTransactionSequence() {
        return traceId.getTransactionSequence();
    }

    @Override
    public long getParentSpanId() {
        return traceId.getParentSpanId();
    }

    @Override
    public short getFlags() {
        return traceId.getFlags();
    }

    @Override
    public boolean isRoot() {
        return traceId.isRoot();
    }

    @Override
    public long getSpanStartTime() {
        return this.traceId.getSpanStartTime();
    }

    @Override
    public TraceId getParentTraceId() {
        return this.traceId.getParentTraceId();
    }

    @Override
    public void close() {
        final AsyncTraceCloseable copy = this.closeable;
        if (copy != null) {
            copy.close();
        }
    }
}
