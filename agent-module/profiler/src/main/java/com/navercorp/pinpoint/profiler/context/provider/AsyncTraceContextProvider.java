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

import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.AsyncTraceContext;
import com.navercorp.pinpoint.profiler.context.BaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.DefaultAsyncTraceContext;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AsyncTraceContextProvider implements Provider<AsyncTraceContext> {

    private final Provider<BaseTraceFactory> baseTraceFactoryProvider;

    @Inject
    public AsyncTraceContextProvider(Provider<BaseTraceFactory> baseTraceFactoryProvider) {
        this.baseTraceFactoryProvider = Objects.requireNonNull(baseTraceFactoryProvider, "baseTraceFactoryProvider");
    }


    @Override
    public AsyncTraceContext get() {
        // Every Guice Provider.get() enters a new InternalContext; the binding is a singleton,
        // so resolve it once and hand the async trace path a plain Supplier.
        Supplier<BaseTraceFactory> baseTraceFactory = Suppliers.memoize(baseTraceFactoryProvider::get);
        return new DefaultAsyncTraceContext(baseTraceFactory);
    }
}
