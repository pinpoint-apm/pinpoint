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


/**
 * @author emeroad
 */
public class SpanEventStackFrame implements StackFrame {
    private final SpanEvent spanEvent;
    private int stackId;
    private Object frameObject;

    public SpanEventStackFrame(SpanEvent spanEvent) {
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }
        this.spanEvent = spanEvent;
    }

    @Override
    public int getStackFrameId() {
        return stackId;
    }

    @Override
    public void setStackFrameId(int stackId) {
        this.stackId = stackId;
    }

    @Override
    public void markBeforeTime() {
        spanEvent.markStartTime();
    }

    @Override
    public long getBeforeTime() {
        return spanEvent.getStartTime();
    }

    @Override
    public void markAfterTime() {
        spanEvent.markAfterTime();
    }

    @Override
    public long getAfterTime() {
        return spanEvent.getAfterTime();
    }

    @Override
    public int getElapsedTime() {
        return spanEvent.getEndElapsed();
    }

    public void setSequence(short sequence) {
        spanEvent.setSequence(sequence);
    }

    public SpanEvent getSpanEvent() {
        return spanEvent;
    }

    @Override
    public void setEndPoint(String endPoint) {
        this.spanEvent.setEndPoint(endPoint);
    }

    @Override
    public void setRpc(String rpc) {
        this.spanEvent.setRpc(rpc);
    }

    @Override
    public void setApiId(int apiId) {
        this.spanEvent.setApiId(apiId);
    }

    @Override
    public void setExceptionInfo(int exceptionId, String exceptionMessage) {
        this.spanEvent.setExceptionInfo(exceptionId, exceptionMessage);
    }

    @Override
    public void setServiceType(short serviceType) {
        spanEvent.setServiceType(serviceType);
    }

    @Override
    public void addAnnotation(Annotation annotation) {
        this.spanEvent.addAnnotation(annotation);
    }

    public void setDestinationId(String destinationId) {
        this.spanEvent.setDestinationId(destinationId);
    }

    public void setNextSpanId(long nextSpanId) {
        this.spanEvent.setNextSpanId(nextSpanId);
    }

    @Override
    public Object attachFrameObject(Object frameObject) {
        Object copy = this.frameObject;
        this.frameObject = frameObject;
        return copy;
    }

    @Override
    public Object getFrameObject() {
        return this.frameObject;
    }

    @Override
    public Object detachFrameObject() {
        Object copy = this.frameObject;
        this.frameObject = null;
        return copy;
    }
    
    @Override
    public short getServiceType() {
        return spanEvent.getServiceType();
    }

    public void setAsyncId(int asyncId) {
        this.spanEvent.setAsyncId(asyncId);
    }
    
    public void setNextAsyncId(int asyncId) {
        this.spanEvent.setNextAsyncId(asyncId);
    }
    
    public void setAsyncSequence(short asyncSequence) {
        this.spanEvent.setAsyncSequence(asyncSequence);
    }
}