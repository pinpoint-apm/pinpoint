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
import com.navercorp.pinpoint.profiler.instrument.ASMEngine;
import com.navercorp.pinpoint.profiler.instrument.DefaultEngineComponent;
import com.navercorp.pinpoint.profiler.instrument.EngineComponent;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.instrument.ScopeFactory;
import com.navercorp.pinpoint.profiler.instrument.classloading.DefineClass;
import com.navercorp.pinpoint.profiler.instrument.classloading.DefineClassFactory;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinitionFactory;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorHolderIdGenerator;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.Instrumentation;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class InstrumentEngineProvider implements Provider<InstrumentEngine> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final InstrumentConfig instrumentConfig;
    private final Provider<ApiMetaDataService> apiMetaDataServiceProvider;
    private final ObjectBinderFactory objectBinderFactory;
    private final Instrumentation instrumentation;
    private final InterceptorHolderIdGenerator interceptorHolderIdGenerator;

    @Inject
    public InstrumentEngineProvider(InstrumentConfig instrumentConfig,
                                    Instrumentation instrumentation,
                                    ObjectBinderFactory objectBinderFactory,
                                    Provider<ApiMetaDataService> apiMetaDataServiceProvider,
                                    InterceptorHolderIdGenerator interceptorHolderIdGenerator) {

        this.instrumentConfig = Objects.requireNonNull(instrumentConfig, "instrumentConfig");
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
        this.objectBinderFactory = Objects.requireNonNull(objectBinderFactory, "objectBinderFactory");
        this.apiMetaDataServiceProvider = Objects.requireNonNull(apiMetaDataServiceProvider, "apiMetaDataServiceProvider");
        this.interceptorHolderIdGenerator = Objects.requireNonNull(interceptorHolderIdGenerator, "interceptorHolderIdGenerator");
    }

    public InstrumentEngine get() {
        final String instrumentEngine = instrumentConfig.getProfileInstrumentEngine().toUpperCase();
        if (InstrumentConfig.INSTRUMENT_ENGINE_ASM.equals(instrumentEngine)) {
            logger.info("ASM InstrumentEngine");

            // WARNING must be singleton
            final InterceptorDefinitionFactory interceptorDefinitionFactory = new InterceptorDefinitionFactory();
            // WARNING must be singleton
            final ScopeFactory scopeFactory = new ScopeFactory();
            EngineComponent engineComponent = new DefaultEngineComponent(objectBinderFactory, interceptorDefinitionFactory, apiMetaDataServiceProvider, scopeFactory, interceptorHolderIdGenerator);
            DefineClass defineClass = DefineClassFactory.getDefineClass();
            return new ASMEngine(instrumentation, engineComponent, defineClass);

        } else {
            logger.warn("Unknown InstrumentEngine:{}", instrumentEngine);

            throw new IllegalArgumentException("Unknown InstrumentEngine:" + instrumentEngine);
        }
    }
}
