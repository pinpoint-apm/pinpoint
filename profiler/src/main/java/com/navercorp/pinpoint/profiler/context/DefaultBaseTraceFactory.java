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

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;
import com.navercorp.pinpoint.profiler.context.id.AsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.id.DefaultAsyncTraceId;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import com.navercorp.pinpoint.profiler.context.id.StatefulAsyncTraceId;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRootFactory;
import com.navercorp.pinpoint.profiler.context.id.ListenableAsyncState;
import com.navercorp.pinpoint.profiler.context.id.TraceIdFactory;
import com.navercorp.pinpoint.profiler.context.recorder.RecorderFactory;
import com.navercorp.pinpoint.profiler.context.storage.AsyncStorage;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;


/**
 * @author emeroad
 * @author Taejin Koo
 */
public class DefaultBaseTraceFactory implements BaseTraceFactory {

    private final CallStackFactory callStackFactory;

    private final StorageFactory storageFactory;
    private final Sampler sampler;

    private final IdGenerator idGenerator;
    private final AsyncIdGenerator asyncIdGenerator;

    private final SpanFactory spanFactory;
    private final RecorderFactory recorderFactory;

    private final TraceRootFactory traceRootFactory;


    public DefaultBaseTraceFactory(TraceRootFactory traceRootFactory, CallStackFactory callStackFactory, StorageFactory storageFactory, Sampler sampler, TraceIdFactory traceIdFactory, IdGenerator idGenerator, AsyncIdGenerator asyncIdGenerator,
                                   SpanFactory spanFactory, RecorderFactory recorderFactory) {
        if (traceRootFactory == null) {
            throw new NullPointerException("traceRootFactory must not be null");
        }
        if (callStackFactory == null) {
            throw new NullPointerException("callStackFactory must not be null");
        }
        if (storageFactory == null) {
            throw new NullPointerException("storageFactory must not be null");
        }
        if (sampler == null) {
            throw new NullPointerException("sampler must not be null");
        }
        if (idGenerator == null) {
            throw new NullPointerException("idGenerator must not be null");
        }
        if (traceIdFactory == null) {
            throw new NullPointerException("traceIdFactory must not be null");
        }
        if (asyncIdGenerator == null) {
            throw new NullPointerException("asyncIdGenerator must not be null");
        }
        if (spanFactory == null) {
            throw new NullPointerException("spanFactory must not be null");
        }
        if (recorderFactory == null) {
            throw new NullPointerException("recorderFactory must not be null");
        }

        this.traceRootFactory = traceRootFactory;
        this.callStackFactory = callStackFactory;
        this.storageFactory = storageFactory;
        this.sampler = sampler;
        this.idGenerator = idGenerator;
        this.asyncIdGenerator = asyncIdGenerator;

        this.spanFactory = spanFactory;
        this.recorderFactory = recorderFactory;
    }


    // continue to trace the request that has been determined to be sampled on previous nodes
    @Override
    public Trace continueTraceObject(final TraceId traceId) {
        // TODO need to modify how to bind a datasender
        // always set true because the decision of sampling has been  made on previous nodes
        // TODO need to consider as a target to sample in case Trace object has a sampling flag (true) marked on previous node.
        final TraceRoot traceRoot = traceRootFactory.continueTraceRoot(traceId);
        final Span span = spanFactory.newSpan(traceRoot);

        final Storage storage = storageFactory.createStorage(traceRoot);
        final CallStack callStack = callStackFactory.newCallStack(traceRoot);

        final Trace trace = new DefaultTrace(span, callStack, storage, asyncIdGenerator, true, recorderFactory);
        return trace;
    }


    @Override
    public Trace continueTraceObject(Trace trace) {
        return trace;
    }


    @Override
    public Trace newTraceObject() {
        // TODO need to modify how to inject a datasender
        final boolean sampling = sampler.isSampling();
        if (sampling) {
            final TraceRoot traceRoot = traceRootFactory.newTraceRoot();
            final Span span = spanFactory.newSpan(traceRoot);

            final Storage storage = storageFactory.createStorage(traceRoot);
            final CallStack callStack = callStackFactory.newCallStack(traceRoot);

            final Trace trace = new DefaultTrace(span, callStack, storage, asyncIdGenerator, true, recorderFactory);

            return trace;
        } else {
            return newDisableTrace();
        }
    }



    // internal async trace.
    @Override
    public Trace continueAsyncTraceObject(AsyncTraceId traceId, int asyncId, long startTime) {

        final TraceRoot traceRoot = getTraceRoot(traceId, startTime);
        final Span span = spanFactory.newSpan(traceRoot);

        final Storage storage = storageFactory.createStorage(traceRoot);

        final Storage asyncStorage = new AsyncStorage(storage);
        final CallStack callStack = callStackFactory.newCallStack(traceRoot);
        // TODO AtomicIdGenerator.UNTRACKED_ID
        final Trace trace = new DefaultTrace(span, callStack, asyncStorage, asyncIdGenerator, true, recorderFactory);

        final AsyncTrace asyncTrace = new AsyncTrace(traceRoot, trace, asyncId, traceId.nextAsyncSequence());

        return asyncTrace;
    }

    private TraceRoot getTraceRoot(AsyncTraceId traceId, long startTime) {
        if (traceId instanceof DefaultAsyncTraceId) {
            final TraceRoot traceRoot = ((DefaultAsyncTraceId) traceId).getTraceRoot();
            assertTraceStartTime(traceRoot, startTime);
            // reuse TraceRoot
            return traceRoot ;
        }

        if (traceId instanceof StatefulAsyncTraceId){
            final TraceRoot traceRoot = ((StatefulAsyncTraceId) traceId).getTraceRoot();
            assertTraceStartTime(traceRoot, startTime);
            // reuse TraceRoot
            return traceRoot ;
        }

        throw new UnsupportedOperationException("unsupported AsyncTraceId:" + traceId);
        // recrated
//        final TraceId parentTraceId = traceId.getParentTraceId();
//        return traceRootFactory.newAsyncTraceRoot(parentTraceId, startTime);
    }

    private void assertTraceStartTime(TraceRoot traceRoot, long startTime) {
        if (traceRoot.getTraceStartTime() != startTime) {
            throw new IllegalStateException("traceStartTime not equals traceRoot:" + traceRoot.getTraceStartTime()
                    + " startTime:" + startTime);
        }
    }

    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace continueAsyncTraceObject(final TraceId traceId) {

        final TraceRoot traceRoot = traceRootFactory.continueTraceRoot(traceId);
        final Span span = spanFactory.newSpan(traceRoot);
        final Storage storage = storageFactory.createStorage(traceRoot);
        final CallStack callStack = callStackFactory.newCallStack(traceRoot);

        final DefaultTrace trace = new DefaultTrace(span, callStack, storage, asyncIdGenerator, true, recorderFactory);

        final SpanAsyncStateListener asyncStateListener = new SpanAsyncStateListener(span, storageFactory.createStorage(traceRoot));
        final ListenableAsyncState stateListener = new ListenableAsyncState(asyncStateListener);
        final AsyncTrace asyncTrace = new AsyncTrace(traceRoot, trace, stateListener);

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

            final Storage storage = storageFactory.createStorage(traceRoot);
            final CallStack callStack = callStackFactory.newCallStack(traceRoot);

            final DefaultTrace trace = new DefaultTrace(span, callStack, storage, asyncIdGenerator, true, recorderFactory);

            final SpanAsyncStateListener asyncStateListener = new SpanAsyncStateListener(span, storageFactory.createStorage(traceRoot));
            final AsyncState closer = new ListenableAsyncState(asyncStateListener);
            final AsyncTrace asyncTrace = new AsyncTrace(traceRoot, trace, closer);

            return asyncTrace;
        } else {
            return newDisableTrace();
        }
    }

    private Trace newDisableTrace() {
        final long nextDisabledId = this.idGenerator.nextDisabledId();
        final Trace disableTrace = new DisableTrace(nextDisabledId);
        return disableTrace;
    }

    @Override
    public Trace disableSampling() {
        final long nextContinuedDisabledId = this.idGenerator.nextContinuedDisabledId();
        final Trace trace = new DisableTrace(nextContinuedDisabledId);
        return trace;
    }

}