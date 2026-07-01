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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.io.SpanVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanEventBo {

    public static final long DEFAULT_START_TIME = -1L;
    public static final long DEFAULT_END_TIME = -1L;

    // version 0 means that the type of prefix's size is int

    private byte version = 0;

    private short sequence;

    private int startElapsed;
    private int endElapsed;
    private long startTime = DEFAULT_START_TIME;
    private long endTime = DEFAULT_END_TIME;

    // private String rpc;
    private int serviceType;

    private String destinationId;
    private String endPoint;
    private int apiId;

    private List<AnnotationBo> annotationBoList;

    private int depth = -1;
    private long nextSpanId = -1;

    private ExceptionInfo exceptionInfo;

    // should get exceptionClass from dao
    private String exceptionClass;

    private int nextAsyncId = -1;

    private List<AttributeBo> attributeBoList = new ArrayList<>();

    public SpanEventBo() {
    }


    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public short getSequence() {
        return sequence;
    }

    public void setSequence(short sequence) {
        this.sequence = sequence;
    }

    /**
     * Returns the event start offset in milliseconds from the parent span/chunk start time.
     */
    public int getStartElapsed() {
        return startElapsed;
    }

    public void setStartElapsed(int startElapsed) {
        this.startElapsed = startElapsed;
    }

    /**
     * Returns the event duration in milliseconds.
     */
    public int getEndElapsed() {
        return endElapsed;
    }

    public void setEndElapsed(int endElapsed) {
        this.endElapsed = endElapsed;
    }

    /**
     * Sets elapsed-only span event time for pre-V3 data.
     * startElapsedMillis is the offset from the parent span/chunk start, and endElapsedMillis
     * is the event duration.
     */
    public void setTraceTime(int version, int startElapsedMillis, int endElapsedMillis) {
        setVersion((byte) version);

        if (version == SpanVersion.TRACE_V3) {
            throw new IllegalArgumentException("TRACE_V3 span event requires absolute start/end time");
        }

        this.startTime = DEFAULT_START_TIME;
        this.endTime = DEFAULT_END_TIME;
        this.startElapsed = startElapsedMillis;
        this.endElapsed = endElapsedMillis;
    }

    /**
     * Sets absolute span event time for TRACE_V3 data.
     * startTime/endTime are epoch nanos. startElapsedMillis is retained as the compatibility
     * offset from the parent span/chunk start; endElapsedMillis is derived from endTime-startTime.
     */
    public void setTraceTime(int version, long startTime, long endTime, int startElapsedMillis) {
        setVersion((byte) version);

        if (version != SpanVersion.TRACE_V3) {
            throw new IllegalArgumentException("absolute start/end time is only supported for TRACE_V3 span events");
        }
        if (endTime < startTime) {
            throw new IllegalArgumentException("span event end time must be greater than or equal to start time");
        }

        this.startTime = startTime;
        this.endTime = endTime;
        this.startElapsed = startElapsedMillis;
        this.endElapsed = (int) TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
    }

    public boolean hasStartTime() {
        return Byte.toUnsignedInt(version) == SpanVersion.TRACE_V3 && startTime != DEFAULT_START_TIME;
    }

    public long getStartTimeNanos() {
        if (!hasStartTime()) {
            throw new IllegalStateException("span event start time is not set");
        }
        return startTime;
    }

    public boolean hasEndTime() {
        return Byte.toUnsignedInt(version) == SpanVersion.TRACE_V3 && endTime != DEFAULT_END_TIME;
    }

    public long getEndTimeNanos() {
        if (!hasEndTime()) {
            throw new IllegalStateException("span event end time is not set");
        }
        return endTime;
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }


    public List<AnnotationBo> getAnnotationBoList() {
        return annotationBoList;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public long getNextSpanId() {
        return nextSpanId;
    }

    public void setNextSpanId(long nextSpanId) {
        this.nextSpanId = nextSpanId;
    }


    public void setAnnotationBoList(List<AnnotationBo> annotationList) {
        if (annotationList == null) {
            return;
        }
        this.annotationBoList = annotationList;
    }

    public void addAnnotation(AnnotationBo annotation) {
        if (annotation == null) {
            return;
        }
        if (this.annotationBoList == null) {
            this.annotationBoList = new ArrayList<>();
        }
        this.annotationBoList.add(annotation);
    }


    public boolean hasException() {
        return exceptionInfo != null;
    }

    public ExceptionInfo getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(ExceptionInfo exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public int getNextAsyncId() {
        return nextAsyncId;
    }

    public void setNextAsyncId(int nextAsyncId) {
        this.nextAsyncId = nextAsyncId;
    }

    public List<AttributeBo> getAttributeBoList() {
        return attributeBoList;
    }

    public void setAttributeBoList(List<AttributeBo> attributeBoList) {
        if (attributeBoList == null) {
            return;
        }
        this.attributeBoList = attributeBoList;
    }

    public void addAttribute(AttributeBo attributeBo) {
        if (attributeBo == null) {
            return;
        }
        if (this.attributeBoList == null) {
            this.attributeBoList = new ArrayList<>();
        }
        this.attributeBoList.add(attributeBo);
    }

    @Override
    public String toString() {
        return "SpanEventBo{" +
                "version=" + version +
                ", sequence=" + sequence +
                ", startElapsed=" + startElapsed +
                ", endElapsed=" + endElapsed +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", serviceType=" + serviceType +
                ", destinationId='" + destinationId + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", apiId=" + apiId +
                ", annotationBoList=" + annotationBoList +
                ", depth=" + depth +
                ", nextSpanId=" + nextSpanId +
                ", exceptionInfo=" + exceptionInfo +
                ", exceptionClass='" + exceptionClass + '\'' +
                ", nextAsyncId=" + nextAsyncId +
                ", attributeBoList=" + attributeBoList +
                '}';
    }

}
