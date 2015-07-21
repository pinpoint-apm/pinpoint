/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;


/**
 * @author Taejin Koo
 */
public class ActiveTrace implements Trace {

    private final Trace trace;
    private final ActiveTraceLifeCycleEventListener eventListener;

    private final ActiveTraceInfo activeTraceInfo;

    public ActiveTrace(Trace trace, ActiveTraceLifeCycleEventListener eventListener) {
        this.trace = trace;
        this.eventListener = eventListener;

        long spanId = -1;
        if (trace != null && trace.getTraceId() != null) {
            TraceId traceId = trace.getTraceId();
            spanId = traceId.getSpanId();
        }

        this.activeTraceInfo = new ActiveTraceInfo(spanId);
        eventListener.onCreate(activeTraceInfo);
    }

    ActiveTraceInfo getActiveTraceInfo() {
        return activeTraceInfo;
    }

    // Trace interfaces... //
    @Override
    public TraceId getTraceId() {
        return trace.getTraceId();
    }

    @Override
    public AsyncTraceId getAsyncTraceId() {
        return trace.getAsyncTraceId();
    }

    @Override
    public boolean canSampled() {
        return trace.canSampled();
    }

    @Override
    public boolean isRoot() {
        return trace.isRoot();
    }

    @Override
    public boolean isAsync() {
        return trace.isAsync();
    }

    @Override
    public SpanRecorder getSpanRecorder() {
        return trace.getSpanRecorder();
    }

    @Override
    public SpanEventRecorder currentSpanEventRecorder() {
        return trace.currentSpanEventRecorder();
    }

    @Override
    public void close() {
        trace.close();
        eventListener.onClose(activeTraceInfo);
    }

    @Override
    public TraceType getTraceType() {
        return trace.getTraceType();
    }

    @Override
    public SpanEventRecorder traceBlockBegin() {
        return trace.traceBlockBegin();
    }


    // StackOperation interfaces... //
    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        return trace.traceBlockBegin(stackId);
    }

    @Override
    public void traceBlockEnd() {
        trace.traceBlockEnd();
    }

    @Override
    public void traceBlockEnd(int stackId) {
        trace.traceBlockEnd(stackId);
    }

    @Override
    public boolean isRootStack() {
        return trace.isRootStack();
    }

    @Override
    public int getCallStackFrameId() {
        return trace.getCallStackFrameId();
    }
    
}