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

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContext;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapperFactory;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionTraceSampler;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class DefaultExceptionRecordingService implements ExceptionRecordingService {

    private final ExceptionTraceSampler exceptionTraceSampler;
    private final ExceptionWrapperFactory exceptionWrapperFactory;
    private final ExceptionContext exceptionContext;

    public DefaultExceptionRecordingService(ExceptionTraceSampler exceptionTraceSampler,
                                            ExceptionWrapperFactory exceptionWrapperFactory,
                                            ExceptionContext exceptionContext
    ) {
        this.exceptionTraceSampler = Objects.requireNonNull(exceptionTraceSampler, "exceptionTraceSampler");
        this.exceptionWrapperFactory = Objects.requireNonNull(exceptionWrapperFactory, "exceptionWrapperFactory");
        this.exceptionContext = Objects.requireNonNull(exceptionContext, "exceptionContext");
    }

    public void recordException(Throwable current, long startTime) {
        final ExceptionContext context = this.exceptionContext;
        ExceptionRecordingState state = context.stateOf(current);
        ExceptionTraceSampler.SamplingState samplingState = getSamplingState(state, context);
        state.checkAndApply(context, current, startTime, samplingState, exceptionWrapperFactory);
    }

    private ExceptionTraceSampler.SamplingState getSamplingState(
            ExceptionRecordingState state,
            ExceptionContext context
    ) {
        if (state.needsNewExceptionId()) {
            return exceptionTraceSampler.isSampled();
        } else if (state.chainContinued()) {
            return exceptionTraceSampler.continuingSampled(context.getSamplingState());
        } else if (state.notNeedExceptionId()) {
            return ExceptionTraceSampler.DISABLED;
        }
        return ExceptionTraceSampler.DISABLED;
    }

    public void recordExceptionIdAnnotation(SpanEvent spanEvent) {
        final ExceptionContext context = this.exceptionContext;
        if (context.hasValidExceptionId()) {
            Annotation<Long> linkId = Annotations.of(AnnotationKey.EXCEPTION_CHAIN_ID.getCode(), context.getExceptionId());
            spanEvent.addAnnotation(linkId);
        }
    }

    @Override
    public void recordException(
            SpanEvent spanEvent,
            Throwable throwable
    ) {
        this.recordException(
                throwable,
                spanEvent.getStartTime()
        );
        this.recordExceptionIdAnnotation(spanEvent);
    }

    @Override
    public void close() {
        this.exceptionContext.flush();
    }
}
