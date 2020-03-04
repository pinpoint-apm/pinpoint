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

import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncTraceContext implements AsyncTraceContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Reference<Trace> EMPTY = DefaultReference.emptyReference();

    private Provider<BaseTraceFactory> baseTraceFactoryProvider;
    private final Binder<Trace> binder;

    public DefaultAsyncTraceContext(Provider<BaseTraceFactory> baseTraceFactoryProvider, Binder<Trace> binder) {
        this.baseTraceFactoryProvider = Assert.requireNonNull(baseTraceFactoryProvider, "baseTraceFactoryProvider");
        this.binder = Assert.requireNonNull(binder, "binder");
    }

    @Override
    public Reference<Trace> continueAsyncTraceObject(TraceRoot traceRoot, LocalAsyncId localAsyncId) {
        final Reference<Trace> reference = checkAndGet();

        final BaseTraceFactory baseTraceFactory = baseTraceFactoryProvider.get();
        final Trace trace = baseTraceFactory.continueAsyncTraceObject(traceRoot, localAsyncId);

        bind(reference, trace);
        return reference;
    }

    @Override
    public Trace newAsyncTraceObject(TraceRoot traceRoot, LocalAsyncId localAsyncId) {
        final BaseTraceFactory baseTraceFactory = baseTraceFactoryProvider.get();
        return baseTraceFactory.continueAsyncTraceObject(traceRoot, localAsyncId);
    }


    @Override
    public Reference<Trace> currentRawTraceObject() {
        final Reference<Trace> reference = binder.get();
        return reference;
    }

    @Override
    public Reference<Trace> currentTraceObject() {
        final Reference<Trace> reference = binder.get();
        final Trace trace = reference.get();
        if (trace == null) {
            return EMPTY;
        }
        if (trace.canSampled()) {
            return reference;
        }
        return EMPTY;
    }


    @Override
    public void removeTraceObject() {
        binder.remove();
    }


    private Reference<Trace> checkAndGet() {
        final Reference<Trace> reference = this.binder.get();
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

    private void bind(Reference<Trace> reference, Trace trace) {
        reference.set(trace);
    }

}
