/*
 * Copyright 2024 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.context.exception;

import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContext;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContextFactory;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapperFactory;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionTraceSampler;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

import java.util.Objects;

public class DefaultExceptionRecordingServiceFactory implements ExceptionRecordingServiceFactory {

    private final ExceptionTraceSampler exceptionTraceSampler;
    private final ExceptionWrapperFactory exceptionWrapperFactory;
    private final ExceptionContextFactory exceptionContextFactory;

    public DefaultExceptionRecordingServiceFactory(ExceptionTraceSampler exceptionTraceSampler,
                                                   ExceptionWrapperFactory exceptionWrapperFactory,
                                                   ExceptionContextFactory exceptionContextFactory) {
        this.exceptionTraceSampler = Objects.requireNonNull(exceptionTraceSampler, "exceptionTraceSampler");
        this.exceptionWrapperFactory = Objects.requireNonNull(exceptionWrapperFactory, "exceptionWrapperFactory");
        this.exceptionContextFactory = Objects.requireNonNull(exceptionContextFactory, "exceptionContextFactory");

    }

    @Override
    public ExceptionRecordingService newService(TraceRoot traceRoot) {
        Objects.requireNonNull(traceRoot, "traceRoot");
        ExceptionContext exceptionContext = this.exceptionContextFactory.newExceptionContext(traceRoot);
        return new DefaultExceptionRecordingService(exceptionTraceSampler, exceptionWrapperFactory, exceptionContext);
    }
}
