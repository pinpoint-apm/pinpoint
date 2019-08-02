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

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.sampler.TraceSampler;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRootFactory;
import com.navercorp.pinpoint.profiler.context.id.ListenableAsyncState;
import com.navercorp.pinpoint.profiler.context.recorder.RecorderFactory;
import com.navercorp.pinpoint.profiler.context.recorder.WrappedSpanEventRecorder;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;


/**
 * @author emeroad
 * @author Taejin Koo
 */
public class DefaultBaseTraceFactory implements BaseTraceFactory {

    private final CallStackFactory<SpanEvent> callStackFactory;

    private final StorageFactory storageFactory;
    private final TraceSampler traceSampler;

    private final SpanFactory spanFactory;
    private final RecorderFactory recorderFactory;

    private final TraceRootFactory traceRootFactory;

    private final ActiveTraceRepository activeTraceRepository;

    public DefaultBaseTraceFactory(TraceRootFactory traceRootFactory, CallStackFactory<SpanEvent> callStackFactory, StorageFactory storageFactory,
                                   TraceSampler traceSampler,
                                   SpanFactory spanFactory, RecorderFactory recorderFactory, ActiveTraceRepository activeTraceRepository) {

        this.traceRootFactory = Assert.requireNonNull(traceRootFactory, "traceRootFactory");
        this.callStackFactory = Assert.requireNonNull(callStackFactory, "callStackFactory");
        this.storageFactory = Assert.requireNonNull(storageFactory, "storageFactory");
        this.traceSampler = Assert.requireNonNull(traceSampler, "traceSampler");

        this.spanFactory = Assert.requireNonNull(spanFactory, "spanFactory");
        this.recorderFactory = Assert.requireNonNull(recorderFactory, "recorderFactory");
        this.activeTraceRepository = Assert.requireNonNull(activeTraceRepository, "activeTraceRepository");
    }

    // continue to trace the request that has been determined to be sampled on previous nodes
    @Override
    public Trace continueTraceObject(final TraceId traceId) {
        // TODO need to modify how to bind a datasender
        // always set true because the decision of sampling has been  made on previous nodes
        // TODO need to consider as a target to sample in case Trace object has a sampling flag (true) marked on previous node.
        // Check max throughput(permits per seconds)
        final TraceSampler.State state = traceSampler.isContinueSampled();
        if (state.isSampled()) {
            final TraceRoot traceRoot = traceRootFactory.continueTraceRoot(traceId, state.nextId());
            final Span span = spanFactory.newSpan(traceRoot);
            final SpanChunkFactory spanChunkFactory = new DefaultSpanChunkFactory(traceRoot);
            final Storage storage = storageFactory.createStorage(spanChunkFactory);
            final CallStack<SpanEvent> callStack = callStackFactory.newCallStack();

            final boolean samplingEnable = true;
            final SpanRecorder spanRecorder = recorderFactory.newSpanRecorder(span, traceId.isRoot(), samplingEnable);
            final WrappedSpanEventRecorder wrappedSpanEventRecorder = recorderFactory.newWrappedSpanEventRecorder(traceRoot);
            final ActiveTraceHandle handle = registerActiveTrace(traceRoot);

            final DefaultTrace trace = new DefaultTrace(span, callStack, storage, samplingEnable, spanRecorder, wrappedSpanEventRecorder, handle);
            return trace;
        } else {
            return newDisableTrace(state.nextId());
        }
    }

    private ActiveTraceHandle registerActiveTrace(TraceRoot traceRoot) {
        return activeTraceRepository.register(traceRoot);
    }

    private ActiveTraceHandle registerActiveTrace(long localTransactionId, long startTime, long threadId) {
        return activeTraceRepository.register(localTransactionId, startTime, threadId);
    }

    @Override
    public Trace newTraceObject() {
        // TODO need to modify how to inject a datasender
        final TraceSampler.State state = traceSampler.isNewSampled();
        final boolean sampling = state.isSampled();
        if (sampling) {
            final TraceRoot traceRoot = traceRootFactory.newTraceRoot(state.nextId());
            final Span span = spanFactory.newSpan(traceRoot);
            final SpanChunkFactory spanChunkFactory = new DefaultSpanChunkFactory(traceRoot);
            final Storage storage = storageFactory.createStorage(spanChunkFactory);
            final CallStack<SpanEvent> callStack = callStackFactory.newCallStack();

            final TraceId traceId = traceRoot.getTraceId();
            final SpanRecorder spanRecorder = recorderFactory.newSpanRecorder(span, traceId.isRoot(), sampling);
            final WrappedSpanEventRecorder wrappedSpanEventRecorder = recorderFactory.newWrappedSpanEventRecorder(traceRoot);

            final ActiveTraceHandle handle = registerActiveTrace(traceRoot);
            final DefaultTrace trace = new DefaultTrace(span, callStack, storage, sampling, spanRecorder, wrappedSpanEventRecorder, handle);

            return trace;
        } else {
            return newDisableTrace(state.nextId());
        }
    }

    // internal async trace.
    @Override
    public Trace continueAsyncTraceObject(TraceRoot traceRoot, LocalAsyncId localAsyncId) {

        final SpanChunkFactory spanChunkFactory = new AsyncSpanChunkFactory(traceRoot, localAsyncId);
        final Storage storage = storageFactory.createStorage(spanChunkFactory);

        final CallStack<SpanEvent> callStack = callStackFactory.newCallStack();

        final boolean samplingEnable = true;
        final SpanRecorder spanRecorder = recorderFactory.newTraceRootSpanRecorder(traceRoot, samplingEnable);

        final WrappedSpanEventRecorder wrappedSpanEventRecorder = recorderFactory.newWrappedSpanEventRecorder(traceRoot);

        final Trace asyncTrace = new AsyncChildTrace(traceRoot, callStack, storage, samplingEnable, spanRecorder, wrappedSpanEventRecorder, localAsyncId);

        return asyncTrace;
    }


    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace continueAsyncTraceObject(final TraceId traceId) {
        final TraceSampler.State state = traceSampler.isContinueSampled();
        final boolean sampling = state.isSampled();
        if (sampling) {
            final TraceRoot traceRoot = traceRootFactory.continueTraceRoot(traceId, state.nextId());
            final Span span = spanFactory.newSpan(traceRoot);

            final SpanChunkFactory spanChunkFactory = new DefaultSpanChunkFactory(traceRoot);
            final Storage storage = storageFactory.createStorage(spanChunkFactory);
            final CallStack<SpanEvent> callStack = callStackFactory.newCallStack();

            final ActiveTraceHandle handle = registerActiveTrace(traceRoot);
            final SpanAsyncStateListener asyncStateListener = new SpanAsyncStateListener(span, storageFactory);
            final AsyncState asyncState = new ListenableAsyncState(asyncStateListener, handle);

            final SpanRecorder spanRecorder = recorderFactory.newSpanRecorder(span, traceId.isRoot(), sampling);
            final WrappedSpanEventRecorder wrappedSpanEventRecorder = recorderFactory.newWrappedSpanEventRecorder(traceRoot, asyncState);


            final DefaultTrace trace = new DefaultTrace(span, callStack, storage, sampling, spanRecorder, wrappedSpanEventRecorder, ActiveTraceHandle.EMPTY_HANDLE);

            final AsyncTrace asyncTrace = new AsyncTrace(traceRoot, trace, asyncState);
            return asyncTrace;
        } else {
            return newDisableTrace(state.nextId());
        }
    }

    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace newAsyncTraceObject() {
        final TraceSampler.State state = traceSampler.isNewSampled();
        final boolean sampling = state.isSampled();
        if (sampling) {
            final TraceRoot traceRoot = traceRootFactory.newTraceRoot(state.nextId());
            final Span span = spanFactory.newSpan(traceRoot);
            final SpanChunkFactory spanChunkFactory = new DefaultSpanChunkFactory(traceRoot);
            final Storage storage = storageFactory.createStorage(spanChunkFactory);
            final CallStack<SpanEvent> callStack = callStackFactory.newCallStack();

            final ActiveTraceHandle handle = registerActiveTrace(traceRoot);
            final SpanAsyncStateListener asyncStateListener = new SpanAsyncStateListener(span, storageFactory);
            final AsyncState asyncState = new ListenableAsyncState(asyncStateListener, handle);


            final TraceId traceId = traceRoot.getTraceId();
            final SpanRecorder spanRecorder = recorderFactory.newSpanRecorder(span, traceId.isRoot(), sampling);
            final WrappedSpanEventRecorder wrappedSpanEventRecorder = recorderFactory.newWrappedSpanEventRecorder(traceRoot, asyncState);


            final DefaultTrace trace = new DefaultTrace(span, callStack, storage, sampling, spanRecorder, wrappedSpanEventRecorder, ActiveTraceHandle.EMPTY_HANDLE);

            final AsyncTrace asyncTrace = new AsyncTrace(traceRoot, trace, asyncState);

            return asyncTrace;
        } else {
            return newDisableTrace(state.nextId());
        }
    }

    @Override
    public Trace disableSampling() {
        final TraceSampler.State state = traceSampler.getContinueDisableState();
        final long nextContinuedDisabledId = state.nextId();
        return newDisableTrace(nextContinuedDisabledId);
    }

    private Trace newDisableTrace(long nextDisabledId) {
        final long traceStartTime = System.currentTimeMillis();
        final long threadId = Thread.currentThread().getId();
        final ActiveTraceHandle activeTraceHandle = registerActiveTrace(nextDisabledId, traceStartTime, threadId);
        final Trace disableTrace = new DisableTrace(nextDisabledId, traceStartTime, activeTraceHandle);
        return disableTrace;
    }
}