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
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.BootstrapJarPaths;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;

import com.navercorp.pinpoint.profiler.instrument.ASMEngine;
import com.navercorp.pinpoint.profiler.instrument.JavassistEngine;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class InstrumentEngineProvider implements Provider<InstrumentEngine> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;
    private final List<String> bootstrapJarPath;
    private final InterceptorRegistryBinder interceptorRegistryBinder;
    private final Provider<ApiMetaDataService> apiMetaDataServiceProvider;
    private final ObjectBinderFactory objectBinderFactory;
    private final Instrumentation instrumentation;

    @Inject
    public InstrumentEngineProvider(ProfilerConfig profilerConfig, Instrumentation instrumentation,
                                    ObjectBinderFactory objectBinderFactory, @BootstrapJarPaths List<String> bootstrapJarPath,
                                    InterceptorRegistryBinder interceptorRegistryBinder, Provider<ApiMetaDataService> apiMetaDataServiceProvider) {

        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.instrumentation = Assert.requireNonNull(instrumentation, "instrumentation must not be null");
        this.objectBinderFactory = Assert.requireNonNull(objectBinderFactory, "objectBinderFactory must not be null");
        this.bootstrapJarPath = Assert.requireNonNull(bootstrapJarPath, "bootstrapJarPath must not be null");
        this.interceptorRegistryBinder = Assert.requireNonNull(interceptorRegistryBinder, "interceptorRegistryBinder must not be null");
        this.apiMetaDataServiceProvider = Assert.requireNonNull(apiMetaDataServiceProvider, "apiMetaDataServiceProvider must not be null");
    }

    public InstrumentEngine get() {
        final String instrumentEngine = profilerConfig.getProfileInstrumentEngine().toUpperCase();
        if (DefaultProfilerConfig.INSTRUMENT_ENGINE_ASM.equals(instrumentEngine)) {
            logger.info("ASM InstrumentEngine.");
            return new ASMEngine(instrumentation, objectBinderFactory, interceptorRegistryBinder, apiMetaDataServiceProvider, bootstrapJarPath);

        } else if (DefaultProfilerConfig.INSTRUMENT_ENGINE_JAVASSIST.equals(instrumentEngine)) {
            logger.info("JAVASSIST InstrumentEngine.");
            return new JavassistEngine(instrumentation, objectBinderFactory, interceptorRegistryBinder, apiMetaDataServiceProvider, bootstrapJarPath);
        } else {
            logger.warn("Unknown InstrumentEngine:{}", instrumentEngine);

            throw new IllegalArgumentException("Unknown InstrumentEngine:" + instrumentEngine);
        }
    }
}
