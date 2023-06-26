/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.context.recorder;


import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.AsyncContextFactory;
import com.navercorp.pinpoint.profiler.context.AsyncId;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanEventFactory;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;
import com.navercorp.pinpoint.profiler.context.errorhandler.IgnoreErrorHandler;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecordingService;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionRecordingContext;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class WrappedSpanEventRecorder extends AbstractRecorder implements SpanEventRecorder {
    private static final Logger logger = LogManager.getLogger(WrappedSpanEventRecorder.class);
    private static final boolean isDebug = logger.isDebugEnabled();

    private final TraceRoot traceRoot;
    private final AsyncContextFactory asyncContextFactory;
    @Nullable
    private final AsyncState asyncState;

    private SpanEvent spanEvent;

    private ExceptionRecordingContext exceptionRecordingContext;

    public WrappedSpanEventRecorder(TraceRoot traceRoot,
                                    AsyncContextFactory asyncContextFactory,
                                    StringMetaDataService stringMetaDataService,
                                    SqlMetaDataService sqlMetaDataService,
                                    IgnoreErrorHandler ignoreErrorHandler,
                                    ExceptionRecordingService exceptionRecordingService) {

        this(traceRoot, asyncContextFactory, null, stringMetaDataService, sqlMetaDataService, ignoreErrorHandler, exceptionRecordingService);
    }

    public WrappedSpanEventRecorder(TraceRoot traceRoot,
                                    AsyncContextFactory asyncContextFactory,
                                    @Nullable final AsyncState asyncState,
                                    final StringMetaDataService stringMetaDataService,
                                    final SqlMetaDataService sqlMetaCacheService,
                                    final IgnoreErrorHandler errorHandler,
                                    final ExceptionRecordingService exceptionRecordingService) {
        super(stringMetaDataService, sqlMetaCacheService, errorHandler, exceptionRecordingService);
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");

        this.asyncContextFactory = Objects.requireNonNull(asyncContextFactory, "asyncContextFactory");
        this.asyncState = asyncState;
    }

    public void setWrapped(final SpanEvent spanEvent, ExceptionRecordingContext exceptionRecordingContext) {
        this.spanEvent = spanEvent;
        this.exceptionRecordingContext = exceptionRecordingContext;
    }

    @Override
    public ParsingResult recordSqlInfo(String sql) {
        if (sql == null) {
            return null;
        }
        ParsingResult parsingResult = sqlMetaDataService.parseSql(sql);
        recordSqlParsingResult(parsingResult);
        return parsingResult;
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult) {
        recordSqlParsingResult(parsingResult, null);
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {
        if (parsingResult == null) {
            return;
        }
        final boolean isNewCache = sqlMetaDataService.cacheSql(parsingResult);
        if (isDebug) {
            if (isNewCache) {
                logger.debug("update sql cache. parsingResult:{}", parsingResult);
            } else {
                logger.debug("cache hit. parsingResult:{}", parsingResult);
            }
        }

        String output = StringUtils.defaultIfEmpty(parsingResult.getOutput(), null);
        bindValue = StringUtils.defaultIfEmpty(bindValue, null);
        final IntStringStringValue sqlValue = new IntStringStringValue(parsingResult.getId(), output, bindValue);

        recordSqlParam(sqlValue);
    }

    private void recordSqlParam(IntStringStringValue intStringStringValue) {
        Annotation<DataType> annotation = Annotations.of(AnnotationKey.SQL_ID.getCode(), intStringStringValue);
        spanEvent.addAnnotation(annotation);
    }

    @Override
    public void recordDestinationId(String destinationId) {
        spanEvent.setDestinationId(destinationId);
    }

    @Override
    public void recordNextSpanId(long nextSpanId) {
        if (nextSpanId == -1) {
            return;
        }
        spanEvent.setNextSpanId(nextSpanId);
    }

    @Override
    public AsyncContext recordNextAsyncContext() {
        final TraceRoot traceRoot = this.traceRoot;
        final AsyncId asyncIdObject = getNextAsyncId();
        // sequence or stack overflow
        final boolean canSampled = isOverflowState();
        return asyncContextFactory.newAsyncContext(traceRoot, asyncIdObject, canSampled);
    }

    // add more conditions to disable asynchronous invocation trace
    protected boolean isOverflowState() {
        return !SpanEventFactory.isDisableSpanEvent(spanEvent);
    }

    @Override
    public AsyncContext recordNextAsyncContext(boolean asyncStateSupport) {
        if (asyncStateSupport && asyncState != null) {
            final AsyncId asyncIdObject = getNextAsyncId();
            final boolean isDisabled = isOverflowState();

            final AsyncState asyncState = this.asyncState;
            asyncState.setup();
            return asyncContextFactory.newAsyncContext(this.traceRoot, asyncIdObject, isDisabled, asyncState);
        }
        return recordNextAsyncContext();
    }


    @Override
    void maskErrorCode(int errorCode) {
        this.traceRoot.getShared().maskErrorCode(errorCode);
    }

    @Override
    void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        this.spanEvent.setExceptionInfo(exceptionClassId, exceptionMessage);
    }

    void recordDetailedException(Throwable throwable) {
        this.exceptionRecordingService.recordException(exceptionRecordingContext, spanEvent, throwable);
    }

    @Override
    public void recordApiId(final int apiId) {
        setApiId0(apiId);
    }

    void setApiId0(final int apiId) {
        spanEvent.setApiId(apiId);
    }

    void addAnnotation(Annotation<?> annotation) {
        spanEvent.addAnnotation(annotation);
    }

    @Override
    public void recordServiceType(ServiceType serviceType) {
        spanEvent.setServiceType(serviceType.getCode());
    }


    @Override
    public void recordEndPoint(String endPoint) {
        spanEvent.setEndPoint(endPoint);
    }

    @Override
    public void recordTime(boolean time) {
        spanEvent.setTimeRecording(time);
        if (time) {
            if (!(spanEvent.getStartTime() == 0)) {
                spanEvent.markStartTime();
            }
        } else {
            spanEvent.setStartTime(0);
            spanEvent.setElapsedTime(0);
        }
    }

    @Override
    public Object detachFrameObject() {
        return spanEvent.detachFrameObject();
    }

    @Override
    public Object getFrameObject() {
        return spanEvent.getFrameObject();
    }

    @Override
    public Object attachFrameObject(Object frameObject) {
        return spanEvent.attachFrameObject(frameObject);
    }

    protected AsyncId getNextAsyncId() {
        AsyncId nextAsyncId = spanEvent.getAsyncIdObject();
        if (nextAsyncId == null) {
            nextAsyncId = asyncContextFactory.newAsyncId();
            spanEvent.setAsyncIdObject(nextAsyncId);
        }
        return nextAsyncId;
    }
}