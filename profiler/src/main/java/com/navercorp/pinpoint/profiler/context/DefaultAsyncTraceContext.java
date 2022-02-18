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
import java.util.Objects;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncTraceContext implements AsyncTraceContext {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final Reference<Trace> EMPTY = DefaultReference.emptyReference();

    private final Provider<BaseTraceFactory> baseTraceFactoryProvider;
    private final Binder<Trace> binder;

    public DefaultAsyncTraceContext(Provider<BaseTraceFactory> baseTraceFactoryProvider, Binder<Trace> binder) {
        this.baseTraceFactoryProvider = Objects.requireNonNull(baseTraceFactoryProvider, "baseTraceFactoryProvider");
        this.binder = Objects.requireNonNull(binder, "binder");
    }

    @Override
    public Reference<Trace> continueAsyncContextTraceObject(TraceRoot traceRoot, LocalAsyncId localAsyncId, boolean canSampled) {
        final Reference<Trace> reference = checkAndGet();

        final Trace trace = newAsyncContextTraceObject(traceRoot, localAsyncId, canSampled);

        bind(reference, trace);
        return reference;
    }

    @Override
    public Trace newAsyncContextTraceObject(TraceRoot traceRoot, LocalAsyncId localAsyncId, boolean canSampled) {
        final BaseTraceFactory baseTraceFactory = baseTraceFactoryProvider.get();
        return baseTraceFactory.continueAsyncContextTraceObject(traceRoot, localAsyncId, canSampled);
    }


    @Override
    public Reference<Trace> currentRawTraceObject() {
        return binder.get();
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
