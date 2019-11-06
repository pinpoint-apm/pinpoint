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
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.AsyncTraceContext;
import com.navercorp.pinpoint.profiler.context.BaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.Binder;
import com.navercorp.pinpoint.profiler.context.DefaultAsyncTraceContext;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AsyncTraceContextProvider implements Provider<AsyncTraceContext> {


    private Provider<BaseTraceFactory> baseTraceFactoryProvider;
    private final Binder<Trace> binder;

    @Inject
    public AsyncTraceContextProvider(Binder<Trace> binder) {
        this.binder = Assert.requireNonNull(binder, "binder");
    }

    @Inject
    public void setBaseTraceFactoryProvider(Provider<BaseTraceFactory> baseTraceFactoryProvider) {
        this.baseTraceFactoryProvider = Assert.requireNonNull(baseTraceFactoryProvider, "baseTraceFactoryProvider");
    }

    @Override
    public AsyncTraceContext get() {
        return new DefaultAsyncTraceContext(baseTraceFactoryProvider,  binder);
    }
}
