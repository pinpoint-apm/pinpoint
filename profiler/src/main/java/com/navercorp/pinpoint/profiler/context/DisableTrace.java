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
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.scope.DefaultTraceScopePool;
import com.navercorp.pinpoint.profiler.context.storage.Storage;


/**
 * @author emeroad
 * @author jaehong.kim
 */
public class DisableTrace implements Trace {

    public static final String UNSUPPORTED_OPERATION  = "disable trace";
    public static final long DISABLE_TRACE_OBJECT_ID = -1;

    private final long id;
    private final long startTime;
    private final DefaultTraceScopePool scopePool = new DefaultTraceScopePool();
    private final ActiveTraceHandle handle;
    private boolean closed = false;

    private final SpanRecorder spanRecorder;
    private final Span span;
    private final Storage storage;

    public DisableTrace(long id, long startTime, ActiveTraceHandle handle, Span span, SpanRecorder spanRecorder, Storage storage) {
        this.id = id;
        this.startTime = startTime;
        this.handle = Assert.requireNonNull(handle, "handle");
        this.span = Assert.requireNonNull(span, "span");
        this.spanRecorder = Assert.requireNonNull(spanRecorder, "spanRecorder");
        this.storage = Assert.requireNonNull(storage, "storage");
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }


    @Override
    public SpanEventRecorder traceBlockBegin() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public void traceBlockEnd() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public void traceBlockEnd(int stackId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public TraceId getTraceId() {
        return this.span.getTraceRoot().getTraceId();

//        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean canSampled() {
        // always return false
        return false;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isRootStack() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }


    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        final long purgeTime = System.currentTimeMillis();
        try {
            span.getWebInfo().setDisabled(true);
            span.setElapsedTime((int) (purgeTime - this.startTime));
            storage.sendWebInfo(span);
        } catch (Throwable t) {
            // 增强的方法发生异常
        } finally {
            closed = true;
            handle.purge(purgeTime);
        }
    }


    @Override
    public int getCallStackFrameId() {
        return 0;
    }

    @Override
    public SpanRecorder getSpanRecorder() {
        return spanRecorder;
    }

    @Override
    public SpanEventRecorder currentSpanEventRecorder() {
        return null;
    }

    @Override
    public TraceScope getScope(String name) {
        return scopePool.get(name);
    }

    @Override
    public TraceScope addScope(String name) {
        return scopePool.add(name);
    }
}