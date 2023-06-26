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
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionRecordingContext;
import com.navercorp.pinpoint.profiler.context.exception.model.SpanEventException;
import com.navercorp.pinpoint.profiler.context.exception.model.SpanEventExceptionFactory;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionTraceSampler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class DefaultExceptionRecordingService implements ExceptionRecordingService {

    private final Logger logger = LogManager.getLogger(getClass());

    private final boolean IS_DEBUG = logger.isDebugEnabled();

    private final ExceptionTraceSampler exceptionTraceSampler;
    private final SpanEventExceptionFactory spanEventExceptionFactory;

    public DefaultExceptionRecordingService(ExceptionTraceSampler exceptionTraceSampler,
            SpanEventExceptionFactory spanEventExceptionFactory
    ) {
        this.exceptionTraceSampler = exceptionTraceSampler;
        this.spanEventExceptionFactory = spanEventExceptionFactory;
    }

    public SpanEventException recordException(ExceptionRecordingContext context, Throwable current, long startTime) {
        Objects.requireNonNull(context);

        ExceptionRecordingState state = ExceptionRecordingState.stateOf(context.getPrevious(), current);
        ExceptionTraceSampler.SamplingState samplingState = getSamplingState(state, context);
        SpanEventException spanEventException = state.checkAndApply(context, current, startTime, samplingState, spanEventExceptionFactory);

        logException(spanEventException);

        return spanEventException;
    }

    private ExceptionTraceSampler.SamplingState getSamplingState(
            ExceptionRecordingState state,
            ExceptionRecordingContext context
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

    private void logException(SpanEventException spanEventException) {
        if (IS_DEBUG && spanEventException != null) {
            logger.debug(spanEventException);
        }
    }

    public Annotation<Long> newExceptionLinkId(ExceptionRecordingContext context) {
        return Annotations.of(AnnotationKey.EXCEPTION_LINK_ID.getCode(), context.getExceptionId());
    }

    @Override
    public void recordException(ExceptionRecordingContext exceptionRecordingContext, SpanEvent spanEvent, Throwable throwable) {
        SpanEventException spanEventException = this.recordException(
                exceptionRecordingContext,
                throwable,
                spanEvent.getStartTime()
        );
        spanEvent.setFlushedException(spanEventException);
        if (exceptionRecordingContext.hasValidExceptionId()) {
            Annotation<Long> linkId = newExceptionLinkId(exceptionRecordingContext);
            spanEvent.addAnnotation(linkId);
        }
    }
}
