/*
 * Copyright 2017 NAVER Corp.
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

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncContext implements AsyncContext {

    private static final AtomicIntegerFieldUpdater<DefaultAsyncContext> ASYNC_SEQUENCE_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(DefaultAsyncContext.class, "asyncSequence");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TraceRoot traceRoot;
    private final int asyncId;

    private final TraceFactory traceFactory;

    @SuppressWarnings("unused")
    private volatile int asyncSequence = 0;

    private final int asyncMethodApiId;


    public DefaultAsyncContext(TraceFactory traceFactory, TraceRoot traceRoot, int asyncId, int asyncMethodApiId) {
        this.traceFactory = Assert.requireNonNull(traceFactory, "traceFactory must not be null");
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot must not be null");
        this.asyncId = asyncId;

        this.asyncMethodApiId = asyncMethodApiId;
    }

    @Override
    public int getAsyncId() {
        return asyncId;
    }


    @Override
    public Trace continueAsyncTraceObject() {

        final Trace nestedTrace = traceFactory.currentRawTraceObject();
        if (nestedTrace != null) {
            // return Nested Trace Object?
            if (nestedTrace.canSampled()) {
                return nestedTrace;
            }
            return null;
        }

        return newAsyncTrace();
    }

    private Trace newAsyncTrace() {
        final short asyncSequence = nextAsyncSequence();
        final Trace asyncTrace = traceFactory.continueAsyncTraceObject(traceRoot, asyncId, asyncSequence);
        if (logger.isDebugEnabled()) {
            logger.debug("traceFactory.continueAsyncTraceObject() AsyncTrace:{}", asyncTrace);
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

//    @Override
//    public Trace continueAsyncTraceObject() {
//
//        final Trace currentTrace = traceFactory.currentRawTraceObject();
//        if (currentTrace == null) {
//            // return Nested Trace?
//            return currentTrace;
//        }
//
//        final short asyncSequence = nextAsyncSequence();
//        final Trace asyncTrace = traceFactory.continueAsyncTraceObject(traceRoot, asyncId, asyncSequence);
//
//        return asyncTrace;
//    }

    @Override
    public Trace currentAsyncTraceObject() {
        final Trace trace = traceFactory.currentTraceObject();
        return trace;
    }


    private short nextAsyncSequence() {
        return (short) ASYNC_SEQUENCE_UPDATER.incrementAndGet(this);
    }

    @Override
    public void close() {
        traceFactory.removeTraceObject();
    }

    @Override
    public String toString() {
        return "DefaultAsyncContext{" +
                "traceRoot=" + traceRoot +
                ", asyncId=" + asyncId +
                ", asyncSequence=" + asyncSequence +
                '}';
    }

}
