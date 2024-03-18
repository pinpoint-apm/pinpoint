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


/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncTraceContext implements AsyncTraceContext {

    private final Provider<BaseTraceFactory> baseTraceFactoryProvider;

    public DefaultAsyncTraceContext(Provider<BaseTraceFactory> baseTraceFactoryProvider) {
        this.baseTraceFactoryProvider = Objects.requireNonNull(baseTraceFactoryProvider, "baseTraceFactoryProvider");
    }

    @Override
    public Trace continueAsyncContextTraceObject(TraceRoot traceRoot, LocalAsyncId localAsyncId) {
        final BaseTraceFactory baseTraceFactory = baseTraceFactoryProvider.get();
        return baseTraceFactory.continueAsyncContextTraceObject(traceRoot, localAsyncId);
    }

    @Override
    public Trace continueDisableAsyncContextTraceObject(LocalTraceRoot traceRoot) {
        final BaseTraceFactory baseTraceFactory = baseTraceFactoryProvider.get();
        return baseTraceFactory.continueDisableAsyncContextTraceObject(traceRoot);
    }


}
