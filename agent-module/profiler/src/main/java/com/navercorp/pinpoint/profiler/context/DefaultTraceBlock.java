/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.DataType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class DefaultTraceBlock implements TraceBlock {
    private final Logger logger = LogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final Trace trace;
    private boolean begin;

    public DefaultTraceBlock(Trace trace) {
        this.trace = Objects.requireNonNull(trace, "trace");
    }

    @Override
    public void close() {
        // AutoCloseable
        if (begin) {
            trace.traceBlockEnd();
        }
    }

    @Override
    public void begin() {
        if (begin) {
            if (logger.isWarnEnabled()) {
                logger.warn("TraceBlock already begin. trace={}", trace);
            }
            return;
        }
        trace.traceBlockBegin();
        begin = true;
    }

    public boolean isBegin() {
        return begin;
    }

    @Override
    public Trace getTrace() {
        return trace;
    }

    @Override
    public ParsingResult recordSqlInfo(String sql) {
        return getSpanEventRecorder().recordSqlInfo(sql);
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult) {
        getSpanEventRecorder().recordSqlParsingResult(parsingResult);
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {
        getSpanEventRecorder().recordSqlParsingResult(parsingResult, bindValue);
    }

    @Override
    public void recordDatabaseInfo(DatabaseInfo databaseInfo, boolean executeQueryType) {
        getSpanEventRecorder().recordDatabaseInfo(databaseInfo, executeQueryType);
    }

    @Override
    public void recordDestinationId(String destinationId) {
        getSpanEventRecorder().recordDestinationId(destinationId);
    }

    @Override
    public void recordNextSpanId(long spanId) {
        getSpanEventRecorder().recordNextSpanId(spanId);
    }

    @Override
    public AsyncContext recordNextAsyncContext() {
        return getSpanEventRecorder().recordNextAsyncContext();
    }

    @Override
    public AsyncContext recordNextAsyncContext(boolean stateful) {
        return getSpanEventRecorder().recordNextAsyncContext(stateful);
    }

    @Override
    public void recordTime(boolean autoTimeRecoding) {
        getSpanEventRecorder().recordTime(autoTimeRecoding);
    }

    @Override
    public void recordException(Throwable throwable) {
        getSpanEventRecorder().recordException(throwable);
    }

    @Override
    public void recordException(boolean markError, Throwable throwable) {
        getSpanEventRecorder().recordException(markError, throwable);
    }

    @Override
    public void recordApiId(int apiId) {
        getSpanEventRecorder().recordApiId(apiId);
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor) {
        getSpanEventRecorder().recordApi(methodDescriptor);
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        getSpanEventRecorder().recordApi(methodDescriptor, args);
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {
        getSpanEventRecorder().recordApi(methodDescriptor, args, index);
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {
        getSpanEventRecorder().recordApi(methodDescriptor, args, start, end);
    }

    @Override
    public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {
        getSpanEventRecorder().recordApiCachedString(methodDescriptor, args, index);
    }

    @Override
    public void recordServiceType(ServiceType serviceType) {
        getSpanEventRecorder().recordServiceType(serviceType);
    }

    @Override
    public void recordEndPoint(String endPoint) {
        getSpanEventRecorder().recordEndPoint(endPoint);
    }

    @Override
    public void recordAttribute(AnnotationKey key, String value) {
        getSpanEventRecorder().recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, int value) {
        getSpanEventRecorder().recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, Integer value) {
        getSpanEventRecorder().recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, long value) {
        getSpanEventRecorder().recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, Long value) {
        getSpanEventRecorder().recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, boolean value) {
        getSpanEventRecorder().recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, double value) {
        getSpanEventRecorder().recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, byte[] value) {
        getSpanEventRecorder().recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, DataType value) {
        getSpanEventRecorder().recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, Object value) {
        getSpanEventRecorder().recordAttribute(key, value);
    }

    @Override
    public Object attachFrameObject(Object frameObject) {
        return getSpanEventRecorder().attachFrameObject(frameObject);
    }

    @Override
    public Object getFrameObject() {
        return getSpanEventRecorder().getFrameObject();
    }

    @Override
    public Object detachFrameObject() {
        return getSpanEventRecorder().detachFrameObject();
    }

    private SpanEventRecorder getSpanEventRecorder() {
        return trace.currentSpanEventRecorder();
    }
}