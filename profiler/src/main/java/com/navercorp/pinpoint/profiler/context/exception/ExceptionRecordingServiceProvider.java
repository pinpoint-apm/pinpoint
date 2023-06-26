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
package com.navercorp.pinpoint.profiler.context.exception;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.exception.id.ExceptionIdGenerator;
import com.navercorp.pinpoint.profiler.context.exception.model.SpanEventExceptionFactory;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionTraceSampler;
import com.navercorp.pinpoint.profiler.context.monitor.config.MonitorConfig;

/**
 * @author intr3p1d
 */
public class ExceptionRecordingServiceProvider implements Provider<ExceptionRecordingService> {

    private final MonitorConfig monitorConfig;
    private final ExceptionIdGenerator exceptionIdGenerator;
    private final ExceptionTraceSampler exceptionTraceSampler;
    private final SpanEventExceptionFactory spanEventExceptionFactory;

    @Inject
    public ExceptionRecordingServiceProvider(
            MonitorConfig monitorConfig,
            ExceptionIdGenerator exceptionIdGenerator,
            ExceptionTraceSampler exceptionTraceSampler,
            SpanEventExceptionFactory spanEventExceptionFactory
    ) {
        this.monitorConfig = monitorConfig;
        this.exceptionIdGenerator = exceptionIdGenerator;
        this.exceptionTraceSampler = exceptionTraceSampler;
        this.spanEventExceptionFactory = spanEventExceptionFactory;
    }

    @Override
    public ExceptionRecordingService get() {
        if (monitorConfig.isExceptionTraceEnable()) {
            return new DefaultExceptionRecordingService(
                    exceptionIdGenerator,
                    exceptionTraceSampler,
                    spanEventExceptionFactory
            );
        } else {
            return DisabledExceptionRecordingService.INSTANCE;
        }
    }
}
