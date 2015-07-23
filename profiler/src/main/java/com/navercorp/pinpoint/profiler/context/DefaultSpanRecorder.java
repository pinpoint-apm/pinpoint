/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * 
 * @author jaehong.kim
 *
 */
public class DefaultSpanRecorder extends AbstractRecorder implements SpanRecorder {
    private final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();
    
    private final Span span;
    private final TraceId traceId;
    private final boolean sampling;
    
    public DefaultSpanRecorder(final TraceContext traceContext, final Span span, final TraceId traceId, final boolean sampling) {
        super(traceContext);

        this.span = span;
        this.traceId = traceId;
        this.sampling = sampling;
    }

    public Span getSpan() {
        return span;
    }
    
    public void recordTraceId(TraceId traceId) {
        span.recordTraceId(traceId);
    }

    @Override
    public void recordStartTime(long startTime) {
        span.setStartTime(startTime);
    }

    @Override
    void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        span.setExceptionInfo(exceptionClassId, exceptionMessage);
        if (!span.isSetErrCode()) {
            span.setErrCode(1);
        }
    }

    @Override
    void recordApiId(int apiId) {
        span.setApiId(apiId);
    }

    @Override
    void addAnnotation(Annotation annotation) {
        span.addAnnotation(annotation);
    }

    @Override
    public void recordServiceType(ServiceType serviceType) {
        span.setServiceType(serviceType.getCode());
    }

    @Override
    public void recordRpcName(String rpc) {
        span.setRpc(rpc);
    }

    @Override
    public void recordRemoteAddress(String remoteAddress) {
        span.setRemoteAddr(remoteAddress);
    }

    @Override
    public void recordEndPoint(String endPoint) {
        span.setEndPoint(endPoint);
    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {
            span.setParentApplicationName(parentApplicationName);
            span.setParentApplicationType(parentApplicationType);
            if (isDebug) {
                logger.debug("ParentApplicationName marked. parentApplicationName={}", parentApplicationName);
            }
    }

    @Override
    public void recordAcceptorHost(String host) {
        span.setAcceptorHost(host); // me
        if (isDebug) {
            logger.debug("Acceptor host received. host={}", host);
        }
    }

    @Override
    public boolean canSampled() {
        return sampling;
    }

    @Override
    public boolean isRoot() {
        return traceId.isRoot();
    }
    
    @Override
    public void recordLogging(LoggingInfo loggingInfo) {
        if (!span.isSetLoggingTransactionInfo()) {
            span.setLoggingTransactionInfo(loggingInfo.getCode()); 
        }
    }
    
    @Override
    public void recordTime(boolean time) {
        span.setTimeRecording(time);
        if(time) {
            if(!span.isSetStartTime()) {
                span.markBeforeTime();
            }
        } else {
            span.setElapsed(0);
            span.setElapsedIsSet(false);
            span.setStartTime(0);
            span.setStartTimeIsSet(false);
        }
    }


    @Override
    public Object attachFrameObject(Object frameObject) {
        return span.attachFrameObject(frameObject);
    }

    @Override
    public Object getFrameObject() {
        return span.getFrameObject();
    }

    @Override
    public Object detachFrameObject() {
        return span.detachFrameObject();
    }
}