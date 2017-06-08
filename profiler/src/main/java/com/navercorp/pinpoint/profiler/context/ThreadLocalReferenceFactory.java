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

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author emeroad
 * @author Taejin Koo
 */
public class ThreadLocalReferenceFactory implements TraceFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final ThreadLocalBinder.ThreadLocalInitializer<ThreadLocalReference<Trace>> THREAD_LOCAL_INITIALIZER = new ThreadLocalBinder.ThreadLocalInitializer<ThreadLocalReference<Trace>>() {
        @Override
        public ThreadLocalReference<Trace> initialValue() {
            return new ThreadLocalTraceReference();
        }
    };

    private final Binder<ThreadLocalReference<Trace>> threadLocalBinder = new ThreadLocalBinder<ThreadLocalReference<Trace>>(THREAD_LOCAL_INITIALIZER);

    private final BaseTraceFactory baseTraceFactory;

    public ThreadLocalReferenceFactory(BaseTraceFactory baseTraceFactory) {
        if (baseTraceFactory == null) {
            throw new NullPointerException("baseTraceFactory must not be null");
        }
        this.baseTraceFactory = baseTraceFactory;

    }


    /**
     * Return Trace object AFTER validating whether it can be sampled or not.
     *
     * @return Trace
     */
    @Override
    public Trace currentTraceObject() {
        final ThreadLocalReference<Trace> reference = threadLocalBinder.get();
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
    public Trace currentRawTraceObject() {
        final ThreadLocalReference<Trace> reference = threadLocalBinder.get();
        return reference.get();
    }

    @Override
    public Trace disableSampling() {
        final ThreadLocalReference<Trace> reference = checkAndGet();
        final Trace trace = this.baseTraceFactory.disableSampling();

        bind(reference, trace);

        return trace;
    }

    // continue to trace the request that has been determined to be sampled on previous nodes
    @Override
    public Trace continueTraceObject(final TraceId traceId) {
        final ThreadLocalReference<Trace> reference = checkAndGet();
        final Trace trace = this.baseTraceFactory.continueTraceObject(traceId);

        bind(reference, trace);
        return trace;
    }


    @Override
    public Trace continueTraceObject(Trace trace) {
        final ThreadLocalReference<Trace> reference = checkAndGet();

        bind(reference, trace);
        return trace;
    }

    private ThreadLocalReference<Trace> checkAndGet() {
        final ThreadLocalReference<Trace> reference = this.threadLocalBinder.get();
        final Trace old = reference.get();
        if (old != null) {
            final PinpointException exception = new PinpointException("already Trace Object exist.");
            if (logger.isWarnEnabled()) {
                logger.warn("beforeTrace:{}", old, exception);
            }
            throw exception;
        }
        return reference;
    }

    @Override
    public Trace newTraceObject() {
        final ThreadLocalReference<Trace> reference = checkAndGet();
        final Trace trace = this.baseTraceFactory.newTraceObject();

        bind(reference, trace);
        return trace;
    }

    private void bind(ThreadLocalReference<Trace> reference, Trace trace) {
        reference.set(trace);

    }

    @Override
    public Trace removeTraceObject() {
        final ThreadLocalReference<Trace> reference = this.threadLocalBinder.get();
        final Trace trace = reference.clear();
        return trace;
    }

    // internal async trace.
    @Override
    public Trace continueAsyncTraceObject(TraceRoot traceRoot, int asyncId, short asyncSequence) {
        final ThreadLocalReference<Trace> reference = checkAndGet();

        final Trace trace = this.baseTraceFactory.continueAsyncTraceObject(traceRoot, asyncId, asyncSequence);

        bind(reference, trace);
        return trace;
    }

    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace continueAsyncTraceObject(final TraceId traceId) {
        final ThreadLocalReference<Trace> reference = checkAndGet();

        final Trace trace = this.baseTraceFactory.continueAsyncTraceObject(traceId);

        bind(reference, trace);
        return trace;
    }

    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace newAsyncTraceObject() {
        final ThreadLocalReference<Trace> reference = checkAndGet();

        final Trace trace = this.baseTraceFactory.newAsyncTraceObject();

        bind(reference, trace);
        return trace;
    }
}