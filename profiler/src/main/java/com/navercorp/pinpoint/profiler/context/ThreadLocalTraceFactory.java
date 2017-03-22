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

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;
import com.navercorp.pinpoint.exception.PinpointException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author emeroad
 * @author Taejin Koo
 */
public class ThreadLocalTraceFactory implements TraceFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Binder<Trace> threadLocalBinder = new ThreadLocalBinder<Trace>();

    private final BaseTraceFactory baseTraceFactory;

    public ThreadLocalTraceFactory(BaseTraceFactory baseTraceFactory) {
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
        final Trace trace = threadLocalBinder.get();
        if (trace == null) {
            return null;
        }
        if (trace.canSampled()) {
            return trace;
        }
        return null;
    }

    /**
     * Return Trace object without validating
     *
     * @return
     */
    @Override
    public Trace currentRpcTraceObject() {
        final Trace trace = threadLocalBinder.get();
        if (trace == null) {
            return null;
        }
        return trace;
    }

    @Override
    public Trace currentRawTraceObject() {
        return threadLocalBinder.get();
    }

    @Override
    public Trace disableSampling() {
        checkBeforeTraceObject();
        final Trace trace = this.baseTraceFactory.disableSampling();

        bind(trace);

        return trace;
    }

    // continue to trace the request that has been determined to be sampled on previous nodes
    @Override
    public Trace continueTraceObject(final TraceId traceId) {
        checkBeforeTraceObject();

        Trace trace = this.baseTraceFactory.continueTraceObject(traceId);

        bind(trace);
        return trace;
    }


    @Override
    public Trace continueTraceObject(Trace trace) {
        checkBeforeTraceObject();

        bind(trace);
        return trace;
    }

    private void checkBeforeTraceObject() {
        final Trace old = this.threadLocalBinder.get();
        if (old != null) {
            final PinpointException exception = new PinpointException("already Trace Object exist.");
            if (logger.isWarnEnabled()) {
                logger.warn("beforeTrace:{}", old, exception);
            }
            throw exception;
        }
    }

    @Override
    public Trace newTraceObject() {
        checkBeforeTraceObject();

        final Trace trace = this.baseTraceFactory.newTraceObject();

        bind(trace);
        return trace;
    }

    private void bind(Trace trace) {
        threadLocalBinder.set(trace);

//        // TODO traceChain example
//        Trace traceChain = new TraceChain(trace);
//        threadLocalBinder.set(traceChain);
//
//        // MetricTraceFactory
//        final Trace delegatedTrace = this.delegate.newTraceObject();
//        if (delegatedTrace instanceof TraceChain) {
//            TraceChain chain = (TraceChain)delegatedTrace;
//            TraceWrap metricTrace = new MetricTraceWrap();
//            // add metricTraceWrap to traceChain
//            chain.addFirst(metricTrace);
//            return chain;
//        } else {
//            logger.warn("error???");
//            return delegatedTrace;
//        }
    }

    @Override
    public Trace removeTraceObject() {
        return this.threadLocalBinder.remove();
    }

    // internal async trace.
    @Override
    public Trace continueAsyncTraceObject(AsyncTraceId traceId, int asyncId, long startTime) {
        checkBeforeTraceObject();

        final Trace trace = this.baseTraceFactory.continueAsyncTraceObject(traceId, asyncId, startTime);

        bind(trace);
        return trace;
    }

    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace continueAsyncTraceObject(final TraceId traceId) {
        checkBeforeTraceObject();

        final Trace trace = this.baseTraceFactory.continueAsyncTraceObject(traceId);

        bind(trace);
        return trace;
    }

    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace newAsyncTraceObject() {
        checkBeforeTraceObject();

        final Trace trace = this.baseTraceFactory.newAsyncTraceObject();

        bind(trace);
        return trace;
    }
}