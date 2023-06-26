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
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.sampler.TraceSampler;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionRecordingContext;
import com.navercorp.pinpoint.profiler.context.id.ListenableAsyncState;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.LoggingAsyncState;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRootFactory;
import com.navercorp.pinpoint.profiler.context.recorder.RecorderFactory;
import com.navercorp.pinpoint.profiler.context.recorder.WrappedSpanEventRecorder;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;


/**
 * @author emeroad
 * @author Taejin Koo
 */
public class DefaultBaseTraceFactory implements BaseTraceFactory {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final CallStackFactory<SpanEvent> callStackFactory;

    private final StorageFactory storageFactory;
    private final TraceSampler traceSampler;

    private final SpanFactory spanFactory;
    private final RecorderFactory recorderFactory;

    private final TraceRootFactory traceRootFactory;

    private final ActiveTraceRepository activeTraceRepository;
    private final UriStatStorage uriStatStorage;

    public DefaultBaseTraceFactory(TraceRootFactory traceRootFactory,
                                   CallStackFactory<SpanEvent> callStackFactory,
                                   StorageFactory storageFactory,
                                   TraceSampler traceSampler,
                                   SpanFactory spanFactory, RecorderFactory recorderFactory,
                                   ActiveTraceRepository activeTraceRepository,
                                   UriStatStorage uriStatStorage) {

        this.traceRootFactory = Objects.requireNonNull(traceRootFactory, "traceRootFactory");
        this.callStackFactory = Objects.requireNonNull(callStackFactory, "callStackFactory");
        this.storageFactory = Objects.requireNonNull(storageFactory, "storageFactory");
        this.traceSampler = Objects.requireNonNull(traceSampler, "traceSampler");

        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");
        this.recorderFactory = Objects.requireNonNull(recorderFactory, "recorderFactory");
        this.activeTraceRepository = Objects.requireNonNull(activeTraceRepository, "activeTraceRepository");
        this.uriStatStorage = Objects.requireNonNull(uriStatStorage, "uriStatStorage");

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
            return newDefaultTrace(traceRoot);
        } else {
            return newLocalTrace(state.nextId());
        }
    }


    @Override
    public Trace newTraceObject() {
        // TODO need to modify how to inject a datasender
        final TraceSampler.State state = traceSampler.isNewSampled();
        if (state.isSampled()) {
            final TraceRoot traceRoot = traceRootFactory.newTraceRoot(state.nextId());
            return newDefaultTrace(traceRoot);
        } else {
            return newLocalTrace(state.nextId());
        }
    }

    @Override
    public Trace newTraceObject(String urlPath) {
        final TraceSampler.State state = traceSampler.isNewSampled(urlPath);
        if (state.isSampled()) {
            final TraceRoot traceRoot = traceRootFactory.newTraceRoot(state.nextId());
            return newDefaultTrace(traceRoot);
        } else {
            return newLocalTrace(state.nextId());
        }
    }


    // internal async trace.
    @Override
    public Trace continueAsyncContextTraceObject(TraceRoot traceRoot, LocalAsyncId localAsyncId) {
        final SpanChunkFactory spanChunkFactory = new AsyncSpanChunkFactory(traceRoot, localAsyncId);
        final Storage storage = storageFactory.createStorage(spanChunkFactory);
        final CallStack<SpanEvent> callStack = callStackFactory.newCallStack();

        final SpanRecorder spanRecorder = recorderFactory.newTraceRootSpanRecorder(traceRoot);
        final WrappedSpanEventRecorder wrappedSpanEventRecorder = recorderFactory.newWrappedSpanEventRecorder(traceRoot);
        final ExceptionRecordingContext exceptionRecordingContext = ExceptionRecordingContext.newContext();

        return new AsyncChildTrace(traceRoot, callStack, storage, spanRecorder, wrappedSpanEventRecorder, exceptionRecordingContext, localAsyncId);
    }

    @Override
    public Trace continueDisableAsyncContextTraceObject(LocalTraceRoot traceRoot) {
        final AsyncState asyncState = newAsyncState(traceRoot, ActiveTraceHandle.EMPTY_HANDLE, ListenableAsyncState.AsyncStateListener.EMPTY);

        SpanRecorder spanRecorder = recorderFactory.newDisableSpanRecorder(traceRoot);
        SpanEventRecorder spanEventRecorder = recorderFactory.newDisableSpanEventRecorder(traceRoot, asyncState);
        return new DisableAsyncChildTrace(traceRoot, spanRecorder, spanEventRecorder);
    }

    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace continueAsyncTraceObject(final TraceId traceId) {
        final TraceSampler.State state = traceSampler.isContinueSampled();
        if (state.isSampled()) {
            final TraceRoot traceRoot = traceRootFactory.continueTraceRoot(traceId, state.nextId());
            return newAsyncDefaultTrace(traceRoot);
        } else {
            return newAsyncLocalTrace(state.nextId());
        }
    }

    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace newAsyncTraceObject() {
        final TraceSampler.State state = traceSampler.isNewSampled();
        if (state.isSampled()) {
            final TraceRoot traceRoot = traceRootFactory.newTraceRoot(state.nextId());
            return newAsyncDefaultTrace(traceRoot);
        } else {
            return newAsyncLocalTrace(state.nextId());
        }
    }

    @Override
    public Trace newAsyncTraceObject(String urlPath) {
        final TraceSampler.State state = traceSampler.isNewSampled(urlPath);
        if (state.isSampled()) {
            final TraceRoot traceRoot = traceRootFactory.newTraceRoot(state.nextId());
            return newAsyncDefaultTrace(traceRoot);
        } else {
            return newAsyncLocalTrace(state.nextId());
        }
    }

    @Override
    public Trace disableSampling() {
        final TraceSampler.State state = traceSampler.getContinueDisableState();
        final long nextContinuedDisabledId = state.nextId();
        return newLocalTrace(nextContinuedDisabledId);
    }

    private DefaultTrace newDefaultTrace(TraceRoot traceRoot) {
        final Span span = spanFactory.newSpan(traceRoot);
        final SpanChunkFactory spanChunkFactory = new DefaultSpanChunkFactory(traceRoot);
        final Storage storage = storageFactory.createStorage(spanChunkFactory);
        final CallStack<SpanEvent> callStack = callStackFactory.newCallStack();

        final SpanRecorder spanRecorder = recorderFactory.newSpanRecorder(span);
        final WrappedSpanEventRecorder wrappedSpanEventRecorder = recorderFactory.newWrappedSpanEventRecorder(traceRoot);
        final ExceptionRecordingContext exceptionRecordingContext = ExceptionRecordingContext.newContext();

        final ActiveTraceHandle handle = registerActiveTrace(traceRoot);
        final CloseListener closeListener = new DefaultCloseListener(traceRoot, handle, uriStatStorage);
        return new DefaultTrace(span, callStack, storage, spanRecorder, wrappedSpanEventRecorder, exceptionRecordingContext, closeListener);
    }

    private AsyncDefaultTrace newAsyncDefaultTrace(TraceRoot traceRoot) {
        final Span span = spanFactory.newSpan(traceRoot);
        final SpanChunkFactory spanChunkFactory = new DefaultSpanChunkFactory(traceRoot);
        final Storage storage = storageFactory.createStorage(spanChunkFactory);
        final CallStack<SpanEvent> callStack = callStackFactory.newCallStack();

        final ActiveTraceHandle handle = registerActiveTrace(traceRoot);
        final SpanAsyncStateListener asyncStateListener = new SpanAsyncStateListener(span, storageFactory);
        final AsyncState asyncState = newAsyncState(traceRoot, handle, asyncStateListener);

        final SpanRecorder spanRecorder = recorderFactory.newSpanRecorder(span);
        final WrappedSpanEventRecorder wrappedSpanEventRecorder = recorderFactory.newWrappedSpanEventRecorder(traceRoot, asyncState);
        final ExceptionRecordingContext exceptionRecordingContext = ExceptionRecordingContext.newContext();

        return new AsyncDefaultTrace(span, callStack, storage, spanRecorder, wrappedSpanEventRecorder, exceptionRecordingContext, asyncState);
    }


    private Trace newLocalTrace(long nextDisabledId) {
        final LocalTraceRoot traceRoot = traceRootFactory.newDisableTraceRoot(nextDisabledId);
        final SpanRecorder spanRecorder = recorderFactory.newDisableSpanRecorder(traceRoot);

        final ActiveTraceHandle handle = registerActiveTrace(traceRoot);
        final CloseListener closeListener = new DefaultCloseListener(traceRoot, handle, uriStatStorage);

        final SpanEventRecorder spanEventRecorder = recorderFactory.newDisableSpanEventRecorder(traceRoot);
        return new DisableTrace(traceRoot, spanRecorder, spanEventRecorder, closeListener);
    }

    private Trace newAsyncLocalTrace(long nextDisabledId) {
        final LocalTraceRoot traceRoot = traceRootFactory.newDisableTraceRoot(nextDisabledId);
        final SpanRecorder spanRecorder = recorderFactory.newDisableSpanRecorder(traceRoot);

        final ActiveTraceHandle handle = registerActiveTrace(traceRoot);
        ListenableAsyncState.AsyncStateListener listener = ListenableAsyncState.AsyncStateListener.EMPTY;
        AsyncState asyncState = newAsyncState(traceRoot, handle, listener);

        final SpanEventRecorder spanEventRecorder = recorderFactory.newDisableSpanEventRecorder(traceRoot, asyncState);
        return new AsyncDisableTrace(traceRoot, spanRecorder, spanEventRecorder, asyncState);
    }

    private AsyncState newAsyncState(LocalTraceRoot traceRoot, ActiveTraceHandle activeTrace, ListenableAsyncState.AsyncStateListener listener) {
        ListenableAsyncState listenableAsyncState = new ListenableAsyncState(traceRoot, listener, activeTrace, uriStatStorage);
        if (logger.isDebugEnabled()) {
            return new LoggingAsyncState(listenableAsyncState);
        }
        return listenableAsyncState;
    }

    private ActiveTraceHandle registerActiveTrace(TraceRoot traceRoot) {
        return activeTraceRepository.register(traceRoot);
    }

    private ActiveTraceHandle registerActiveTrace(LocalTraceRoot localTraceRoot) {
        return activeTraceRepository.register(localTraceRoot);
    }
}