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
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;
import com.navercorp.pinpoint.thrift.dto.TSpan;

/**
 * Span represent RPC
 *
 * @author netspider
 * @author emeroad
 */
public class Span extends TSpan implements FrameAttachment {
    private boolean timeRecording = true;
    private Object frameObject;

    private final TraceRoot traceRoot;

    public Span(final TraceRoot traceRoot) {
        if (traceRoot == null) {
            throw new NullPointerException("traceRoot must not be null");
        }
        this.traceRoot = traceRoot;

        final TraceId traceId = traceRoot.getTraceId();
        this.setSpanId(traceId.getSpanId());
        final long parentSpanId = traceId.getParentSpanId();
        if (parentSpanId != SpanId.NULL) {
            this.setParentSpanId(parentSpanId);
        }
        this.setFlag(traceId.getFlags());
    }

    public TraceRoot getTraceRoot() {
        return traceRoot;
    }

    public void markBeforeTime() {
        final long spanStartTime = traceRoot.getTraceStartTime();
        this.setStartTime(spanStartTime);
    }

    public void markAfterTime() {
        markAfterTime(System.currentTimeMillis());
    }

    public void markAfterTime(long currentTime) {
        final int after = (int)(currentTime - this.getStartTime());

        // TODO  have to change int to long
        if (after != 0) {
            this.setElapsed(after);
        }
    }

    public long getAfterTime() {
        return this.getStartTime() + this.getElapsed();
    }


    public void addAnnotation(Annotation annotation) {
        this.addToAnnotations(annotation);
    }

    public void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        final TIntStringValue exceptionInfo = new TIntStringValue(exceptionClassId);
        if (StringUtils.hasLength(exceptionMessage)) {
            exceptionInfo.setStringValue(exceptionMessage);
        }
        super.setExceptionInfo(exceptionInfo);
    }

    public boolean isSetErrCode() {
        return isSetErr();
    }

    public int getErrCode() {
        return getErr();
    }

    public void setErrCode(int exception) {
        super.setErr(exception);
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

    public void finish() {
        // snapshot last image
        final Shared shared = traceRoot.getShared();
        final int errorCode = shared.getErrorCode();
        this.setErrCode(errorCode);

        final byte loggingInfo = shared.getLoggingInfo();
        this.setLoggingTransactionInfo(loggingInfo);
    }
}