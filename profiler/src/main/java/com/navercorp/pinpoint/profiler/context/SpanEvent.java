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

import com.navercorp.pinpoint.bootstrap.context.FrameAttachment;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

/**
 * Span represent RPC
 *
 * @author netspider
 * @author emeroad
 */
public class SpanEvent extends TSpanEvent implements FrameAttachment {

    private final TraceRoot traceRoot;
    private int stackId;
    private boolean timeRecording = true;
    private Object frameObject;
    private long startTime;
    private long afterTime;

    public SpanEvent(TraceRoot traceRoot) {
        if (traceRoot == null) {
            throw new NullPointerException("traceRoot must not be null");
        }
        this.traceRoot = traceRoot;
    }


    public TraceRoot getTraceRoot() {
        return traceRoot;
    }

    public void addAnnotation(Annotation annotation) {
        this.addToAnnotations(annotation);
    }

    public void setExceptionInfo(boolean markError, int exceptionClassId, String exceptionMessage) {
        setExceptionInfo(exceptionClassId, exceptionMessage);
        if (markError) {
            traceRoot.getShared().maskErrorCode(1);
        }
    }

    void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        final TIntStringValue exceptionInfo = new TIntStringValue(exceptionClassId);
        if (StringUtils.hasLength(exceptionMessage)) {
            exceptionInfo.setStringValue(exceptionMessage);
        }
        super.setExceptionInfo(exceptionInfo);
    }


    public void markStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    public long getStartTime() {
        return startTime;
    }

    public void markAfterTime() {
        this.afterTime = System.currentTimeMillis();

    }

    public long getAfterTime() {
        return afterTime;
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

    @Override
    public Object attachFrameObject(Object attachObject) {
        final Object before = this.frameObject;
        this.frameObject = attachObject;
        return before;
    }

    @Override
    public Object getFrameObject() {
        return this.frameObject;
    }

    @Override
    public Object detachFrameObject() {
        final Object delete = this.frameObject;
        this.frameObject = null;
        return delete;
    }
}
