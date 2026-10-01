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
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

import java.util.Objects;
import java.util.function.Supplier;


/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncTraceContext implements AsyncTraceContext {

    // Supplied lazily: BaseTraceFactory -> RecorderFactory -> AsyncContextFactory -> AsyncTraceContext -> BaseTraceFactory
    // is a construction cycle, so the factory cannot be injected directly. The supplier is expected to be memoized.
    private final Supplier<BaseTraceFactory> baseTraceFactorySupplier;

    public DefaultAsyncTraceContext(Supplier<BaseTraceFactory> baseTraceFactorySupplier) {
        this.baseTraceFactorySupplier = Objects.requireNonNull(baseTraceFactorySupplier, "baseTraceFactorySupplier");
    }

    @Override
    public Trace continueAsyncContextTraceObject(TraceRoot traceRoot, LocalAsyncId localAsyncId, boolean asyncTraceBlock) {
        final BaseTraceFactory baseTraceFactory = baseTraceFactorySupplier.get();
        return baseTraceFactory.continueAsyncContextTraceObject(traceRoot, localAsyncId, asyncTraceBlock);
    }

    @Override
    public Trace continueDisableAsyncContextTraceObject(LocalTraceRoot traceRoot) {
        final BaseTraceFactory baseTraceFactory = baseTraceFactorySupplier.get();
        return baseTraceFactory.continueDisableAsyncContextTraceObject(traceRoot);
    }


}
