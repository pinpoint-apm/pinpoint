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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.AsyncContextFactory;
import com.navercorp.pinpoint.profiler.context.AsyncTraceContext;
import com.navercorp.pinpoint.profiler.context.DefaultAsyncContextFactory;
import com.navercorp.pinpoint.profiler.context.id.AsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.method.PredefinedMethodDescriptorRegistry;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AsyncContextFactoryProvider implements Provider<AsyncContextFactory> {

    private final Provider<AsyncTraceContext> asyncTraceContextProvider;
    private final AsyncIdGenerator asyncIdGenerator;
    private final PredefinedMethodDescriptorRegistry predefinedMethodDescriptorRegistry;

    @Inject
    public AsyncContextFactoryProvider(Provider<AsyncTraceContext> asyncTraceContextProvider, AsyncIdGenerator asyncIdGenerator, PredefinedMethodDescriptorRegistry predefinedMethodDescriptorRegistry) {
        this.asyncTraceContextProvider = Assert.requireNonNull(asyncTraceContextProvider, "asyncTraceContextProvider");
        this.asyncIdGenerator = Assert.requireNonNull(asyncIdGenerator, "asyncIdGenerator");
        this.predefinedMethodDescriptorRegistry = Assert.requireNonNull(predefinedMethodDescriptorRegistry, "predefinedMethodDescriptorRegistry");
    }



    @Override
    public AsyncContextFactory get() {
        final AsyncTraceContext asyncTraceContext = asyncTraceContextProvider.get();
        return new DefaultAsyncContextFactory(asyncTraceContext, asyncIdGenerator, predefinedMethodDescriptorRegistry);
    }
}
