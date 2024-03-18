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
import com.navercorp.pinpoint.profiler.context.exception.DefaultExceptionRecorderFactory;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecorderFactory;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContextFactory;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapperFactory;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionChainSampler;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ExceptionRecorderFactoryProvider implements Provider<ExceptionRecorderFactory> {

    private final ExceptionChainSampler exceptionChainSampler;
    private final ExceptionWrapperFactory exceptionWrapperFactory;
    private final ExceptionContextFactory exceptionContextFactory;

    @Inject
    public ExceptionRecorderFactoryProvider(
            ExceptionChainSampler exceptionChainSampler,
            ExceptionWrapperFactory exceptionWrapperFactory,
            ExceptionContextFactory exceptionContextFactory
    ) {
        this.exceptionChainSampler = Objects.requireNonNull(exceptionChainSampler, "exceptionTraceSampler");
        this.exceptionWrapperFactory = Objects.requireNonNull(exceptionWrapperFactory, "exceptionWrapperFactory");
        this.exceptionContextFactory = Objects.requireNonNull(exceptionContextFactory, "exceptionContextFactory");
    }

    @Override
    public ExceptionRecorderFactory get() {
        return new DefaultExceptionRecorderFactory(
                exceptionChainSampler,
                exceptionWrapperFactory,
                exceptionContextFactory
        );
    }
}
