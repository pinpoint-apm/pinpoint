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
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncContext implements AsyncContext {


    private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncContext.class);

    private final TraceRoot traceRoot;
    private final AsyncId asyncId;

    private final AsyncTraceContext asyncTraceContext;

    private final int asyncMethodApiId;


    public DefaultAsyncContext(AsyncTraceContext asyncTraceContext, TraceRoot traceRoot, AsyncId asyncId, int asyncMethodApiId) {
        this.asyncTraceContext = Assert.requireNonNull(asyncTraceContext, "asyncTraceContext");
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot");
        this.asyncId = Assert.requireNonNull(asyncId, "asyncId");


        this.asyncMethodApiId = asyncMethodApiId;
    }


    public TraceRoot getTraceRoot() {
        return traceRoot;
    }

    @Override
    public Trace continueAsyncTraceObject() {

        final Reference<Trace> reference = asyncTraceContext.currentRawTraceObject();
        final Trace nestedTrace = reference.get();
        if (nestedTrace != null) {
            // return Nested Trace Object?
            if (nestedTrace.canSampled()) {
                return nestedTrace;
            }
            return null;
        }

        return newAsyncTrace(reference);
    }

    private Trace newAsyncTrace(Reference<Trace> reference) {
//        final int asyncId = this.asyncId.getAsyncId();
//        final short asyncSequence = this.asyncId.nextAsyncSequence();
        final LocalAsyncId localAsyncId = this.asyncId.nextLocalAsyncId();
        final Trace asyncTrace = asyncTraceContext.newAsyncTraceObject(traceRoot, localAsyncId);


        bind(reference, asyncTrace);

        if (logger.isDebugEnabled()) {
            logger.debug("asyncTraceContext.continueAsyncTraceObject() AsyncTrace:{}", asyncTrace);
        }

        // add async scope.
        final TraceScope oldScope = asyncTrace.addScope(ASYNC_TRACE_SCOPE);
        if (oldScope != null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Duplicated async trace scope={}.", oldScope.getName());
            }
            // delete corrupted trace.
//            deleteAsyncTrace(trace);
            return null;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("start async trace scope");
            }
        }

        // first block.
        final SpanEventRecorder recorder = asyncTrace.currentSpanEventRecorder();
        recorder.recordServiceType(ServiceType.ASYNC);
        recorder.recordApiId(asyncMethodApiId);

        return asyncTrace;
    }

    private void bind(Reference<Trace> reference, Trace asyncTrace) {
        Assert.state(reference.get() == null, "traceReference is  null");

        reference.set(asyncTrace);
    }


    @Override
    public Trace currentAsyncTraceObject() {
        final Reference<Trace> reference = asyncTraceContext.currentTraceObject();
        return reference.get();
    }


    @Override
    public void close() {
        asyncTraceContext.removeTraceObject();
    }

    @Override
    public String toString() {
        return "DefaultAsyncContext{" +
                "traceRoot=" + traceRoot +
                ", asyncId=" + asyncId +
                '}';
    }

}
