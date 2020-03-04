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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * Span represent RPC
 *
 * @author netspider
 * @author emeroad
 */
public class Span extends DefaultFrameAttachment {
    private boolean timeRecording = true;

    private final TraceRoot traceRoot;

    private long startTime; // required
    private int elapsedTime; // optional

    private int apiId; // optional
    private short serviceType; // required

    private List<Annotation> annotations; // optional
    private List<SpanEvent> spanEventList; // optional

    private String remoteAddr; // optional

    private String parentApplicationName; // optional
    private short parentApplicationType; // optional
    private String acceptorHost; // optional

    private IntStringValue exceptionInfo; // optional


    public Span(final TraceRoot traceRoot) {
        if (traceRoot == null) {
            throw new NullPointerException("traceRoot");
        }
        this.traceRoot = traceRoot;
    }

    public TraceRoot getTraceRoot() {
        return traceRoot;
    }


    public long getStartTime() {
        return startTime;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public short getServiceType() {
        return serviceType;
    }

    public void setServiceType(short serviceType) {
        this.serviceType = serviceType;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }


    public List<SpanEvent> getSpanEventList() {
        return spanEventList;
    }

    public void setSpanEventList(List<SpanEvent> spanEventList) {
        this.spanEventList = spanEventList;
    }

    public String getParentApplicationName() {
        return parentApplicationName;
    }

    public void setParentApplicationName(String parentApplicationName) {
        this.parentApplicationName = parentApplicationName;
    }

    public short getParentApplicationType() {
        return parentApplicationType;
    }

    public void setParentApplicationType(short parentApplicationType) {
        this.parentApplicationType = parentApplicationType;
    }

    public String getAcceptorHost() {
        return acceptorHost;
    }

    public void setAcceptorHost(String acceptorHost) {
        this.acceptorHost = acceptorHost;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public IntStringValue getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(IntStringValue exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public void markBeforeTime() {
        final long spanStartTime = traceRoot.getTraceStartTime();
        this.setStartTime(spanStartTime);
    }

    public void setStartTime(long spanStartTime) {
        this.startTime = spanStartTime;
    }

    public void markAfterTime() {
        markAfterTime(System.currentTimeMillis());
    }

    public void markAfterTime(long currentTime) {
        final int after = (int)(currentTime - this.getStartTime());
        this.setElapsedTime(after);
    }

    public void addAnnotation(Annotation annotation) {
        if (this.annotations == null) {
            this.annotations = new ArrayList<Annotation>();
        }
        this.annotations.add(annotation);
    }

    public void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        final IntStringValue exceptionInfo = new IntStringValue(exceptionClassId, exceptionMessage);
        this.setExceptionInfo(exceptionInfo);
    }


    public boolean isTimeRecording() {
        return timeRecording;
    }

    public void setTimeRecording(boolean timeRecording) {
        this.timeRecording = timeRecording;
    }

    public void finish() {
        // snapshot last image
        final Shared shared = traceRoot.getShared();
        if (shared.getStatusCode() != 0) {
            Annotation annotation = new Annotation(AnnotationKey.HTTP_STATUS_CODE.getCode(), shared.getStatusCode());
            this.addAnnotation(annotation);
        }
    }

    public void clear() {

    }

    @Override
    public String toString() {
        return "Span{" +
                "timeRecording=" + timeRecording +
                ", traceRoot=" + traceRoot +
                ", startTime=" + startTime +
                ", elapsed=" + elapsedTime +
                ", serviceType=" + serviceType +
                ", remoteAddr='" + remoteAddr + '\'' +
                ", annotations=" + annotations +
                ", spanEventList=" + spanEventList +
                ", parentApplicationName='" + parentApplicationName + '\'' +
                ", parentApplicationType=" + parentApplicationType +
                ", acceptorHost='" + acceptorHost + '\'' +
                ", apiId=" + apiId +
                ", exceptionInfo=" + exceptionInfo +
                "} " + super.toString();
    }
}