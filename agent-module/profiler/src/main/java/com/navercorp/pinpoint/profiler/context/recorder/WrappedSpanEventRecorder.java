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
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.AsyncContextFactory;
import com.navercorp.pinpoint.profiler.context.AsyncId;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanEventFactory;
import com.navercorp.pinpoint.profiler.context.SqlCountService;
import com.navercorp.pinpoint.profiler.context.errorhandler.IgnoreErrorHandler;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecorder;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class WrappedSpanEventRecorder extends AbstractRecorder implements SpanEventRecorder, AutoCloseable {
    private static final Logger logger = LogManager.getLogger(WrappedSpanEventRecorder.class);
    private static final boolean isDebug = logger.isDebugEnabled();

    private final SqlCountService sqlCountService;

    private final TraceRoot traceRoot;
    private final AsyncContextFactory asyncContextFactory;
    @Nullable
    private final AsyncState asyncState;

    private SpanEvent spanEvent;

    public WrappedSpanEventRecorder(TraceRoot traceRoot,
                                    AsyncContextFactory asyncContextFactory,
                                    StringMetaDataService stringMetaDataService,
                                    SqlMetaDataService sqlMetaDataService,
                                    IgnoreErrorHandler ignoreErrorHandler,
                                    ExceptionRecorder exceptionRecorder,
                                    SqlCountService sqlCountService) {
        this(traceRoot, asyncContextFactory, null, stringMetaDataService, sqlMetaDataService, ignoreErrorHandler, exceptionRecorder, sqlCountService);
    }

    public WrappedSpanEventRecorder(TraceRoot traceRoot,
                                    AsyncContextFactory asyncContextFactory,
                                    @Nullable final AsyncState asyncState,
                                    final StringMetaDataService stringMetaDataService,
                                    final SqlMetaDataService sqlMetaCacheService,
                                    final IgnoreErrorHandler errorHandler,
                                    final ExceptionRecorder exceptionRecorder,
                                    final SqlCountService sqlCountService) {
        super(stringMetaDataService, sqlMetaCacheService, errorHandler, exceptionRecorder);
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");

        this.asyncContextFactory = Objects.requireNonNull(asyncContextFactory, "asyncContextFactory");
        this.asyncState = asyncState;
        this.sqlCountService = Objects.requireNonNull(sqlCountService, "sqlCountService");
    }

    public void setWrapped(final SpanEvent spanEvent) {
        this.spanEvent = spanEvent;
    }

    @Override
    public ParsingResult recordSqlInfo(String sql) {
        if (sql == null) {
            return null;
        }
        ParsingResult parsingResult = sqlMetaDataService.wrapSqlResult(sql);
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

        Annotation<?> sqlAnnotation = this.sqlMetaDataService.newSqlAnnotation(parsingResult, bindValue);
        spanEvent.addAnnotation(sqlAnnotation);

        if (spanEvent.isExecuteQueryType()) {
            sqlCountService.recordSqlCount(this.traceRoot.getShared());
        }
    }

    @Override
    public void recordDatabaseInfo(DatabaseInfo databaseInfo, boolean executeQueryType) {
        this.spanEvent.setExecuteQueryType(executeQueryType);
        recordServiceType(executeQueryType ? databaseInfo.getExecuteQueryType() : databaseInfo.getType());
        recordEndPoint(databaseInfo.getMultipleHost());
        recordDestinationId(databaseInfo.getDatabaseId());
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
        this.exceptionRecorder.recordException(spanEvent, throwable);
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

    @Override
    public void close() {
        exceptionRecorder.close();
    }
}