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

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;

/**
 * @author emeroad
 */
public class TraceChain implements TraceWrap {

    private Trace delegate;

    public TraceChain(Trace trace) {
        if (trace == null) {
            throw new NullPointerException("trace must not be null");
        }
        this.delegate = trace;
    }

    public void addFirst(TraceWrap trace) {
        if (trace == null) {
            throw new NullPointerException("trace must not be null");
        }
        final Trace last = this.delegate;
        trace.wrap(last);
        this.delegate = trace;
    }

    public void wrap(Trace trace) {
        throw new UnsupportedOperationException("TraceChainWrap.wrap() unsupported.");
    }


    @Override
    public Trace unwrap() {
        final Trace delegate = this.delegate;
        if (delegate instanceof TraceWrapper) {
            return ((TraceWrapper) delegate).unwrap();
        }
        return delegate;
    }

    @Override
    public long getId() {
        return this.delegate.getId();
    }

    @Override
    public long getStartTime() {
        return this.delegate.getStartTime();
    }

    @Override
    public Thread getBindThread() {
        return this.delegate.getBindThread();
    }

    @Override
    public TraceId getTraceId() {
        return delegate.getTraceId();
    }

    @Override
    public AsyncTraceId getAsyncTraceId() {
        return delegate.getAsyncTraceId();
    }

    @Override
    public AsyncTraceId getAsyncTraceId(boolean closeable) {
        return delegate.getAsyncTraceId(closeable);
    }

    @Override
    public boolean canSampled() {
        return delegate.canSampled();
    }

    @Override
    public boolean isRoot() {
        return delegate.isRoot();
    }

    @Override
    public boolean isAsync() {
        return delegate.isAsync();
    }

    @Override
    public SpanRecorder getSpanRecorder() {
        return delegate.getSpanRecorder();
    }

    @Override
    public SpanEventRecorder currentSpanEventRecorder() {
        return delegate.currentSpanEventRecorder();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public TraceScope getScope(String name) {
        return delegate.getScope(name);
    }

    @Override
    public TraceScope addScope(String name) {
        return delegate.addScope(name);
    }

    @Override
    public SpanEventRecorder traceBlockBegin() {
        return delegate.traceBlockBegin();
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        return delegate.traceBlockBegin(stackId);
    }

    @Override
    public void traceBlockEnd() {
        delegate.traceBlockEnd();
    }

    @Override
    public void traceBlockEnd(int stackId) {
        delegate.traceBlockEnd(stackId);
    }

    @Override
    public boolean isRootStack() {
        return delegate.isRootStack();
    }

    @Override
    public int getCallStackFrameId() {
        return delegate.getCallStackFrameId();
    }
}