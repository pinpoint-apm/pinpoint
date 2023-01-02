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

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.scope.DefaultTraceScopePool;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;

import java.util.Objects;


/**
 * @author emeroad
 * @author jaehong.kim
 */
public class DisableTrace implements Trace {

    public static final String UNSUPPORTED_OPERATION  = "disable trace";
    public static final long DISABLE_TRACE_OBJECT_ID = -1;

    private final LocalTraceRoot traceRoot;
    private final SpanRecorder spanRecorder;
    private DefaultTraceScopePool scopePool;
    private final ActiveTraceHandle handle;
    private final UriStatStorage uriStatStorage;

    private boolean closed = false;

    public DisableTrace(LocalTraceRoot traceRoot, SpanRecorder spanRecorder, ActiveTraceHandle handle, UriStatStorage uriStatStorage) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.spanRecorder = Objects.requireNonNull(spanRecorder, "spanRecorder");

        this.handle = Objects.requireNonNull(handle, "handle");
        this.uriStatStorage = Objects.requireNonNull(uriStatStorage, "uriStatStorage");
    }

    @Override
    public long getId() {
        return traceRoot.getLocalTransactionId();
    }

    @Override
    public long getStartTime() {
        return traceRoot.getTraceStartTime();
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
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
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
        closed = true;


        final long purgeTime = System.currentTimeMillis();
        handle.purge(purgeTime);
        boolean status = getStatus();
        String uriTemplate = getShared().getUriTemplate();
        uriStatStorage.store(uriTemplate, status, traceRoot.getTraceStartTime(), purgeTime);
    }

    private boolean getStatus() {
        return getShared().getErrorCode() == 0;
    }

    private Shared getShared() {
        return traceRoot.getShared();
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
        if (scopePool == null) {
            return null;
        }
        return scopePool.get(name);
    }

    @Override
    public TraceScope addScope(String name) {
        if (scopePool == null) {
            scopePool = new DefaultTraceScopePool();
        }
        return scopePool.add(name);
    }

}