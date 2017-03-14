/*
 * Copyright 2017 NAVER Corp.
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


import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.DefaultTrace;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.thrift.dto.TIntStringStringValue;

/**
 * 
 * @author jaehong.kim
 *
 */
public class WrappedSpanEventRecorder extends AbstractRecorder implements SpanEventRecorder {
    private final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private SpanEvent spanEvent;

    public WrappedSpanEventRecorder(final StringMetaDataService stringMetaDataService, final SqlMetaDataService sqlMetaCacheService) {
        super(stringMetaDataService, sqlMetaCacheService);
    }

    public void setWrapped(final SpanEvent spanEvent) {
        this.spanEvent = spanEvent;
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

        final TIntStringStringValue tSqlValue = new TIntStringStringValue(parsingResult.getId());
        final String output = parsingResult.getOutput();
        if (isNotEmpty(output)) {
            tSqlValue.setStringValue1(output);
        }
        if (isNotEmpty(bindValue)) {
            tSqlValue.setStringValue2(bindValue);
        }
        recordSqlParam(tSqlValue);
    }

    private static boolean isNotEmpty(final String bindValue) {
        return bindValue != null && !bindValue.isEmpty();
    }

    private void recordSqlParam(TIntStringStringValue tIntStringStringValue) {
        spanEvent.addAnnotation(new Annotation(AnnotationKey.SQL_ID.getCode(), tIntStringStringValue));
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
    public void recordAsyncId(int asyncId) {
        spanEvent.setAsyncId(asyncId);
    }

    @Override
    public void recordNextAsyncId(int nextAsyncId) {
        spanEvent.setNextAsyncId(nextAsyncId);
    }

    @Override
    public void recordAsyncSequence(short asyncSequence) {
        spanEvent.setAsyncSequence(asyncSequence);
    }

    @Override
    void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        this.setExceptionInfo(true, exceptionClassId, exceptionMessage);
    }

    @Override
    void setExceptionInfo(boolean markError, int exceptionClassId, String exceptionMessage) {
        spanEvent.setExceptionInfo(exceptionClassId, exceptionMessage);
        if (markError) {
            final Span span = spanEvent.getSpan();
            if (!span.isSetErrCode()) {
                span.setErrCode(1);
            }
        }
    }

    @Override
    public void recordApiId(final int apiId) {
        setApiId0(apiId);
    }

    void setApiId0(final int apiId) {
        spanEvent.setApiId(apiId);
    }

    void addAnnotation(Annotation annotation) {
        spanEvent.addAnnotation(annotation);
    }

    @Override
    public void recordServiceType(ServiceType serviceType) {
        spanEvent.setServiceType(serviceType.getCode());
    }

    @Override
    public void recordRpcName(String rpc) {
        spanEvent.setRpc(rpc);
    }

    @Override
    public void recordEndPoint(String endPoint) {
        spanEvent.setEndPoint(endPoint);
    }

    @Override
    public void recordTime(boolean time) {
        spanEvent.setTimeRecording(time);
        if (time) {
            if(!spanEvent.isSetStartElapsed()) {
                spanEvent.markStartTime();
            }
        } else {
            spanEvent.setEndElapsed(0);
            spanEvent.setEndElapsedIsSet(false);
            spanEvent.setStartElapsed(0);
            spanEvent.setStartElapsedIsSet(false);
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
}