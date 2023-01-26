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
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.id.AsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.method.PredefinedMethodDescriptorRegistry;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncContextFactory implements AsyncContextFactory {

    private final AsyncTraceContext asyncTraceContext;
    private final Binder<Trace> binder;
    private final AsyncIdGenerator asyncIdGenerator;
    private final PredefinedMethodDescriptorRegistry predefinedMethodDescriptorRegistry;
    private final int asyncMethodApiId;

    public DefaultAsyncContextFactory(AsyncTraceContext asyncTraceContext,
                                      Binder<Trace> binder,
                                      AsyncIdGenerator asyncIdGenerator,
                                      PredefinedMethodDescriptorRegistry predefinedMethodDescriptorRegistry) {
        this.asyncTraceContext = Objects.requireNonNull(asyncTraceContext, "traceFactoryProvider");
        this.asyncIdGenerator = Objects.requireNonNull(asyncIdGenerator, "asyncIdGenerator");
        this.binder = Objects.requireNonNull(binder, "binder");

        this.predefinedMethodDescriptorRegistry = Objects.requireNonNull(predefinedMethodDescriptorRegistry, "predefinedMethodDescriptorRegistry");

        this.asyncMethodApiId = getAsyncMethodApiId(predefinedMethodDescriptorRegistry);
    }

    private int getAsyncMethodApiId(PredefinedMethodDescriptorRegistry predefinedMethodDescriptorRegistry) {
        final MethodDescriptor asyncMethodDescriptor = predefinedMethodDescriptorRegistry.getAsyncMethodDescriptor();
        return asyncMethodDescriptor.getApiId();
    }

    @Override
    public AsyncId newAsyncId() {
        return asyncIdGenerator.newAsyncId();
    }

    @Override
    public AsyncContext newAsyncContext(TraceRoot traceRoot, AsyncId asyncId, boolean canSampled) {
        Objects.requireNonNull(traceRoot, "traceRoot");
        Objects.requireNonNull(asyncId, "asyncId");

        if (canSampled) {
            return new DefaultAsyncContext(asyncTraceContext, binder, traceRoot, asyncId, this.asyncMethodApiId);
        } else {
            return newDisableAsyncContext(traceRoot);
        }
    }

    @Override
    public AsyncContext newAsyncContext(TraceRoot traceRoot, AsyncId asyncId, boolean canSampled, AsyncState asyncState) {
        Objects.requireNonNull(traceRoot, "traceRoot");
        Objects.requireNonNull(asyncId, "asyncId");
        Objects.requireNonNull(asyncState, "asyncState");

        if (canSampled) {
            return new StatefulAsyncContext(asyncTraceContext, binder, traceRoot, asyncId, asyncMethodApiId, asyncState);
        } else {
            // TODO
            return newDisableAsyncContext(traceRoot, asyncState);
        }

    }

    @Override
    public AsyncContext newDisableAsyncContext(LocalTraceRoot traceRoot) {
        return new DisableAsyncContext(asyncTraceContext, binder, traceRoot);
    }

    @Override
    public AsyncContext newDisableAsyncContext(LocalTraceRoot traceRoot, AsyncState asyncState) {
        return new StatefulDisableAsyncContext(asyncTraceContext, binder, traceRoot, asyncState);
    }

}
