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
import java.util.Objects;
import com.navercorp.pinpoint.profiler.instrument.DefaultEngineComponent;
import com.navercorp.pinpoint.profiler.instrument.EngineComponent;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;

import com.navercorp.pinpoint.profiler.instrument.ASMEngine;
import com.navercorp.pinpoint.profiler.instrument.ScopeFactory;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinitionFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

/**
 * @author Woonduk Kang(emeroad)
 */
public class InstrumentEngineProvider implements Provider<InstrumentEngine> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InstrumentConfig instrumentConfig;
    private final InterceptorRegistryBinder interceptorRegistryBinder;
    private final Provider<ApiMetaDataService> apiMetaDataServiceProvider;
    private final ObjectBinderFactory objectBinderFactory;
    private final Instrumentation instrumentation;

    @Inject
    public InstrumentEngineProvider(InstrumentConfig instrumentConfig,
                                    Instrumentation instrumentation,
                                    ObjectBinderFactory objectBinderFactory,
                                    InterceptorRegistryBinder interceptorRegistryBinder,
                                    Provider<ApiMetaDataService> apiMetaDataServiceProvider) {

        this.instrumentConfig = Objects.requireNonNull(instrumentConfig, "instrumentConfig");
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.objectBinderFactory = Objects.requireNonNull(objectBinderFactory, "objectBinderFactory");
        this.interceptorRegistryBinder = Objects.requireNonNull(interceptorRegistryBinder, "interceptorRegistryBinder");
        this.apiMetaDataServiceProvider = Objects.requireNonNull(apiMetaDataServiceProvider, "apiMetaDataServiceProvider");
    }

    public InstrumentEngine get() {
        final String instrumentEngine = instrumentConfig.getProfileInstrumentEngine().toUpperCase();
        if (InstrumentConfig.INSTRUMENT_ENGINE_ASM.equals(instrumentEngine)) {
            logger.info("ASM InstrumentEngine");

            // WARNING must be singleton
            final InterceptorDefinitionFactory interceptorDefinitionFactory = new InterceptorDefinitionFactory();
            // WARNING must be singleton
            final ScopeFactory scopeFactory = new ScopeFactory();
            EngineComponent engineComponent = new DefaultEngineComponent(objectBinderFactory, interceptorRegistryBinder, interceptorDefinitionFactory, apiMetaDataServiceProvider, scopeFactory);
            return new ASMEngine(instrumentation, engineComponent);

        } else {
            logger.warn("Unknown InstrumentEngine:{}", instrumentEngine);

            throw new IllegalArgumentException("Unknown InstrumentEngine:" + instrumentEngine);
        }
    }
}
