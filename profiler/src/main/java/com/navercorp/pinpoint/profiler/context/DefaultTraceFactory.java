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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.exception.PinpointException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author emeroad
 * @author Taejin Koo
 */
public class DefaultTraceFactory implements TraceFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Binder<Trace> threadLocalBinder;

    private final BaseTraceFactory baseTraceFactory;

    public DefaultTraceFactory(BaseTraceFactory baseTraceFactory, Binder<Trace> binder) {
        this.baseTraceFactory = Assert.requireNonNull(baseTraceFactory, "baseTraceFactory");
        this.threadLocalBinder = Assert.requireNonNull(binder, "binder");
    }

    /**
     * Return Trace object AFTER validating whether it can be sampled or not.
     *
     * @return Trace
     */
    @Override
    public Trace currentTraceObject() {
        final Reference<Trace> reference = threadLocalBinder.get();
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
        final Reference<Trace> reference = threadLocalBinder.get();
        return reference.get();
    }

    @Override
    public Trace disableSampling() {
        final Reference<Trace> reference = checkAndGet();
        final Trace trace = this.baseTraceFactory.disableSampling();

        bind(reference, trace);

        return trace;
    }

    // continue to trace the request that has been determined to be sampled on previous nodes
    @Override
    public Trace continueTraceObject(final TraceId traceId) {
        final Reference<Trace> reference = checkAndGet();
        final Trace trace = this.baseTraceFactory.continueTraceObject(traceId);

        bind(reference, trace);
        return trace;
    }


    @Override
    public Trace continueTraceObject(Trace trace) {
        final Reference<Trace> reference = checkAndGet();

        bind(reference, trace);
        return trace;
    }

    private Reference<Trace> checkAndGet() {
        final Reference<Trace> reference = this.threadLocalBinder.get();
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
        final Reference<Trace> reference = checkAndGet();
        final Trace trace = this.baseTraceFactory.newTraceObject();

        bind(reference, trace);
        return trace;
    }

    private void bind(Reference<Trace> reference, Trace trace) {
        reference.set(trace);

    }

    @Override
    public Trace removeTraceObject() {
        final Reference<Trace> reference = this.threadLocalBinder.get();
        final Trace trace = reference.clear();
        return trace;
    }


    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace continueAsyncTraceObject(final TraceId traceId) {
        final Reference<Trace> reference = checkAndGet();

        final Trace trace = this.baseTraceFactory.continueAsyncTraceObject(traceId);

        bind(reference, trace);
        return trace;
    }

    // entry point async trace.
    @InterfaceAudience.LimitedPrivate("vert.x")
    @Override
    public Trace newAsyncTraceObject() {
        final Reference<Trace> reference = checkAndGet();

        final Trace trace = this.baseTraceFactory.newAsyncTraceObject();

        bind(reference, trace);
        return trace;
    }
}