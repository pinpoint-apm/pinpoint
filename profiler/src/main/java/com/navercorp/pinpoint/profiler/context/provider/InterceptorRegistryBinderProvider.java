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

package com.navercorp.pinpoint.profiler.context.provider;


import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Objects;

import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;
import com.navercorp.pinpoint.profiler.interceptor.registry.DefaultInterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;

/**
 * @author Woonduk Kang(emeroad)
 */
public class InterceptorRegistryBinderProvider implements Provider<InterceptorRegistryBinder>  {

    private final InterceptorRegistryBinder interceptorRegistryBinder;

    @Inject
    public InterceptorRegistryBinderProvider(InstrumentConfig instrumentConfig) {
        this(getInterceptorRegistrySize(instrumentConfig));
    }

    private static int getInterceptorRegistrySize(InstrumentConfig instrumentConfig) {
        Objects.requireNonNull(instrumentConfig, "instrumentConfig");
        return instrumentConfig.getInterceptorRegistrySize();
    }

    public InterceptorRegistryBinderProvider(int interceptorSize) {
        this.interceptorRegistryBinder = new DefaultInterceptorRegistryBinder(interceptorSize);
    }

    @Override
    public InterceptorRegistryBinder get() {
        return interceptorRegistryBinder;
    }
}
