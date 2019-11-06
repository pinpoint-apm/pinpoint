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

import com.navercorp.pinpoint.common.util.IntStringValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Span represent RPC
 *
 * @author netspider
 * @author emeroad
 */
public class SpanEvent extends DefaultFrameAttachment {

    private boolean timeRecording = true;
    private int stackId;

    private long startTime;
    private int elapsedTime;

    private short sequence; // required

//    private String rpc; // optional
    private short serviceType; // required
    private String endPoint; // optional

    private List<Annotation> annotations; // optional
    private int depth = -1; // optional

    private long nextSpanId = -1; // optional
    private String destinationId; // optional

    private int apiId; // optional
    private IntStringValue exceptionInfo; // optional

    private AsyncId asyncIdObject;

    public SpanEvent() {
    }

    public void addAnnotation(Annotation annotation) {
        if (this.annotations == null) {
            this.annotations = new ArrayList<Annotation>();
        }
        this.annotations.add(annotation);
    }

    public void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        final IntStringValue exceptionInfo = new IntStringValue(exceptionClassId, exceptionMessage);
        this.exceptionInfo = exceptionInfo;
    }

    public void markStartTime() {
        setStartTime(System.currentTimeMillis());
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void markAfterTime() {
        checkStartTime();
        setAfterTime(System.currentTimeMillis());
    }


    public void setAfterTime(long afterTime) {
        checkStartTime();
        this.elapsedTime = (int) (afterTime - startTime);
    }

    private void checkStartTime() {
        if (startTime == 0) {
            throw new IllegalStateException("startTime not recorded");
        }
    }

    public long getAfterTime() {
        return startTime + elapsedTime;
    }

    public int getStackId() {
        return stackId;
    }

    public void setStackId(int stackId) {
        this.stackId = stackId;
    }

    public boolean isTimeRecording() {
        return timeRecording;
    }

    public void setTimeRecording(boolean timeRecording) {
        this.timeRecording = timeRecording;
    }

    public short getSequence() {
        return sequence;
    }

    public void setSequence(short sequence) {
        this.sequence = sequence;
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

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
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

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
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

    @Deprecated
    public LocalAsyncId getLocalAsyncId() {
        return null;
    }


    public void setAsyncIdObject(AsyncId asyncIdObject) {
        this.asyncIdObject = asyncIdObject;
    }

    public AsyncId getAsyncIdObject() {
        return asyncIdObject;
    }

    @Override
    public String toString() {
        return "SpanEvent{" +
                "stackId=" + stackId +
                ", timeRecording=" + timeRecording +
                ", startTime=" + startTime +
                ", elapsedTime=" + elapsedTime +
                ", asyncIdObject=" + asyncIdObject +
                ", sequence=" + sequence +
                ", serviceType=" + serviceType +
                ", endPoint='" + endPoint + '\'' +
                ", annotations=" + annotations +
                ", depth=" + depth +
                ", nextSpanId=" + nextSpanId +
                ", destinationId='" + destinationId + '\'' +
                ", apiId=" + apiId +
                ", exceptionInfo=" + exceptionInfo +
                "} ";
    }
}
