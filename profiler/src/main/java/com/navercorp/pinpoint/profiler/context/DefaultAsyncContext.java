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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncContext implements AsyncContext {


    private static final Logger logger = LogManager.getLogger(DefaultAsyncContext.class);

    private final TraceRoot traceRoot;
    private final AsyncId asyncId;

    private final AsyncContexts.Remote remote;

    @Nullable
    private final AsyncState asyncState;

    DefaultAsyncContext(AsyncContexts.Remote remote,
                        TraceRoot traceRoot,
                        AsyncId asyncId,
                        @Nullable AsyncState asyncState) {
        this.remote = Objects.requireNonNull(remote, "remote");

        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.asyncId = Objects.requireNonNull(asyncId, "asyncId");
        this.asyncState = asyncState;
    }


    public TraceRoot getTraceRoot() {
        return traceRoot;
    }

    @Override
    public Trace continueAsyncTraceObject() {

        final Reference<Trace> reference = remote.binder().get();
        final Trace nestedTrace = reference.get();
        if (nestedTrace != null) {
            // return Nested Trace Object?
            if (nestedTrace.canSampled()) {
                return nestedTrace;
            }
            return null;
        }

        return newAsyncContextTrace(reference);
    }

    private Trace newAsyncContextTrace(Reference<Trace> reference) {
//        final int asyncId = this.asyncId.getAsyncId();
//        final short asyncSequence = this.asyncId.nextAsyncSequence();
        final LocalAsyncId localAsyncId = this.asyncId.nextLocalAsyncId();
        final Trace asyncTrace = remote.asyncTraceContext().continueAsyncContextTraceObject(traceRoot, localAsyncId);

        bind(reference, asyncTrace);

        if (logger.isDebugEnabled()) {
            logger.debug("asyncTraceContext.continuAsyncTraceObject(e) AsyncTrace:{}", asyncTrace);
        }

        if (AsyncScopeUtils.nested(asyncTrace, ASYNC_TRACE_SCOPE)) {
            return null;
        }

        // first block.
        final SpanEventRecorder recorder = asyncTrace.currentSpanEventRecorder();
        if (recorder != null) {
            recorder.recordServiceType(ServiceType.ASYNC);
            recorder.recordApiId(remote.asyncMethodApiId());
        }

        return asyncTrace;
    }

    private void bind(Reference<Trace> reference, Trace asyncTrace) {
        Assert.state(reference.get() == null, "traceReference is  null");

        reference.set(asyncTrace);
    }


    @Override
    public Trace currentAsyncTraceObject() {
        final Reference<Trace> reference = remote.binder().get();
        final Trace trace = reference.get();
        if (trace == null) {
            return null;
        }
        if (trace.canSampled()) {
            return trace;
        }
        return null;
    }


    @Override
    public void close() {
       remote.binder().remove();
    }

    @Override
    public boolean finish() {
        final AsyncState copy = this.asyncState;
        if (copy != null) {
            copy.finish();
            return true;
        }
        return false;
    }

    @Nullable
    public AsyncState getAsyncState() {
        return asyncState;
    }

    @Override
    public String toString() {
        return "DefaultAsyncContext{" +
                "asyncId=" + asyncId +
                ", traceRoot=" + traceRoot +
                ", asyncState=" + asyncState +
                '}';
    }
}
