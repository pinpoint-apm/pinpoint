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

    private final AsyncContexts.Remote remote;
    private final AsyncContexts.Local local;
    private final AsyncIdGenerator asyncIdGenerator;
    public DefaultAsyncContextFactory(AsyncTraceContext asyncTraceContext,
                                      Binder<Trace> binder,
                                      AsyncIdGenerator asyncIdGenerator,
                                      PredefinedMethodDescriptorRegistry predefinedMethodDescriptorRegistry) {
        int asyncMethodApiId = getAsyncMethodApiId(predefinedMethodDescriptorRegistry);
        this.remote = AsyncContexts.remote(asyncTraceContext, binder, asyncMethodApiId);
        this.local = AsyncContexts.local(asyncTraceContext, binder);

        this.asyncIdGenerator = Objects.requireNonNull(asyncIdGenerator, "asyncIdGenerator");
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
            return remote.sync(traceRoot, asyncId);
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
            return remote.async(traceRoot, asyncState, asyncId);
        } else {
            // TODO
            return newDisableAsyncContext(traceRoot, asyncState);
        }

    }

    @Override
    public AsyncContext newDisableAsyncContext(LocalTraceRoot traceRoot) {
        return local.sync(traceRoot);
    }

    @Override
    public AsyncContext newDisableAsyncContext(LocalTraceRoot traceRoot, AsyncState asyncState) {
        return local.async(traceRoot, asyncState);
    }

}
