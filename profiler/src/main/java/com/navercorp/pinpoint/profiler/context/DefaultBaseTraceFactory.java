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
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
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
    private final Sampler sampler;

    private final IdGenerator idGenerator;

    private final SpanFactory spanFactory;
    private final RecorderFactory recorderFactory;

    private final TraceRootFactory traceRootFactory;

    private final ActiveTraceRepository activeTraceRepository;


    public DefaultBaseTraceFactory(TraceRootFactory traceRootFactory, CallStackFactory<SpanEvent> callStackFactory, StorageFactory storageFactory,
                                   Sampler sampler, IdGenerator idGenerator,
                                   SpanFactory spanFactory, RecorderFactory recorderFactory, ActiveTraceRepository activeTraceRepository) {

        this.traceRootFactory = Assert.requireNonNull(traceRootFactory, "traceRootFactory must not be null");
        this.callStackFactory = Assert.requireNonNull(callStackFactory, "callStackFactory must not be null");
        this.storageFactory = Assert.requireNonNull(storageFactory, "storageFactory must not be null");
        this.sampler = Assert.requireNonNull(sampler, "sampler must not be null");
        this.idGenerator = Assert.requireNonNull(idGenerator, "idGenerator must not be null");

        this.spanFactory = Assert.requireNonNull(spanFactory, "spanFactory must not be null");
        this.recorderFactory = Assert.requireNonNull(recorderFactory, "recorderFactory must not be null");
        this.activeTraceRepository = Assert.requireNonNull(activeTraceRepository, "activeTraceRepository must not be null");
    }


    // continue to trace the request that has been determined to be sampled on previous nodes
    @Override
    public Trace continueTraceObject(final TraceId traceId) {
        // TODO need to modify how to bind a datasender
        // always set true because the decision of sampling has been  made on previous nodes
        // TODO need to consider as a target to sample in case Trace object has a sampling flag (true) marked on previous node.
        final TraceRoot traceRoot = traceRootFactory.continueTraceRoot(traceId);
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
        final boolean sampling = sampler.isSampling();
        if (sampling) {
            final TraceRoot traceRoot = traceRootFactory.newTraceRoot();
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
            return newDisableTrace();
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

        final boolean sampling = true;

        final TraceRoot traceRoot = traceRootFactory.continueTraceRoot(traceId);
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
    }

    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace newAsyncTraceObject() {

        final boolean sampling = sampler.isSampling();
        if (sampling) {

            final TraceRoot traceRoot = traceRootFactory.newTraceRoot();
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
            return newDisableTrace();
        }
    }

    private Trace newDisableTrace() {
        final long nextDisabledId = this.idGenerator.nextDisabledId();

        return newDisableTrace0(nextDisabledId);
    }

    @Override
    public Trace disableSampling() {
        final long nextContinuedDisabledId = this.idGenerator.nextContinuedDisabledId();
        return newDisableTrace0(nextContinuedDisabledId);
    }

    private Trace newDisableTrace0(long id) {
        final long traceStartTime = System.currentTimeMillis();
        final long threadId = Thread.currentThread().getId();
        final ActiveTraceHandle activeTraceHandle = registerActiveTrace(id, traceStartTime, threadId);
        final Trace disableTrace = new DisableTrace(id, traceStartTime, activeTraceHandle);
        return disableTrace;
    }

}