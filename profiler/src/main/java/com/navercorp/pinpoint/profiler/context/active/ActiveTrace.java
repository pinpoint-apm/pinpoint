/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.active;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.profiler.context.recorder.DefaultSpanRecorder;
import com.navercorp.pinpoint.profiler.context.Span;

/**
 * @author HyunGil Jeong
 */
public class ActiveTrace {

    private final Trace trace;

    public ActiveTrace(Trace trace) {
        if (trace == null) {
            throw new NullPointerException("trace must not be null");
        }
        this.trace = trace;
    }

    public long getStartTime() {
        return this.trace.getStartTime();
    }

    public long getId() {
        return this.trace.getId();
    }

    public Thread getBindThread() {
        return this.trace.getBindThread();
    }

    public boolean isSampled() {
        return trace.canSampled();
    }

    public String getTransactionId() {
        if (!trace.canSampled()) {
            return null;
        }

        TraceId traceId = trace.getTraceId();
        if (traceId == null) {
            return null;
        }

        return traceId.getTransactionId();
    }

    public String getEntryPoint() {
        if (!trace.canSampled()) {
            return null;
        }

        SpanRecorder spanRecorder = trace.getSpanRecorder();
        if (!(spanRecorder instanceof DefaultSpanRecorder)) {
            return null;
        }

        Span span = ((DefaultSpanRecorder) spanRecorder).getSpan();
        if (span == null) {
            return null;
        }

        return span.getRpc();
    }

}
