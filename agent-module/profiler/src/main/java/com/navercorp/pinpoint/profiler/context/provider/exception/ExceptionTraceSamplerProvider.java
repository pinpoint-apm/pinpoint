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
package com.navercorp.pinpoint.profiler.context.provider.exception;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionChainSampler;
import com.navercorp.pinpoint.profiler.context.monitor.config.ExceptionTraceConfig;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ExceptionTraceSamplerProvider implements Provider<ExceptionChainSampler> {
    private final ExceptionTraceConfig exceptionTraceConfig;

    @Inject
    public ExceptionTraceSamplerProvider(ExceptionTraceConfig exceptionTraceConfig) {
        this.exceptionTraceConfig = Objects.requireNonNull(exceptionTraceConfig, "exceptionTraceConfig");
    }

    @Override
    public ExceptionChainSampler get() {
        return new ExceptionChainSampler(exceptionTraceConfig.getExceptionTraceNewThroughput());
    }
}
