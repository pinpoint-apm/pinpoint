/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.navercorp.pinpoint.profiler.context.provider.exception.DisabledExceptionContextFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.exception.DisabledExceptionRecordingServiceProvider;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecordingService;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContextFactory;
import com.navercorp.pinpoint.profiler.context.monitor.config.ExceptionTraceConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class DisabledExceptionTraceModule extends PrivateModule {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ExceptionTraceConfig exceptionTraceConfig;

    public DisabledExceptionTraceModule(ExceptionTraceConfig exceptionTraceConfig) {
        this.exceptionTraceConfig = Objects.requireNonNull(exceptionTraceConfig, "exceptionTraceConfig");
    }


    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());
        bind(ExceptionTraceConfig.class).toInstance(exceptionTraceConfig);

        bind(ExceptionContextFactory.class).toProvider(DisabledExceptionContextFactoryProvider.class).in(Scopes.SINGLETON);
        expose(ExceptionContextFactory.class);

        bind(ExceptionRecordingService.class).toProvider(DisabledExceptionRecordingServiceProvider.class).in(Scopes.SINGLETON);
        expose(ExceptionRecordingService.class);
    }
}
