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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceRootSpanRecorder implements SpanRecorder {

    private final TraceRoot traceRoot;
    private final boolean sampling;

    public TraceRootSpanRecorder(TraceRoot traceRoot, boolean sampling) {
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot");

        this.sampling = sampling;
    }

    @Override
    public boolean canSampled() {
        return sampling;
    }

    @Override
    public boolean isRoot() {
        return traceRoot.getTraceId().isRoot();
    }

    @Override
    public void recordStartTime(long startTime) {

    }

    @Override
    public void recordTime(boolean autoTimeRecoding) {

    }

    @Override
    public void recordError() {
        traceRoot.getShared().maskErrorCode(1);
    }

    @Override
    public void recordException(Throwable throwable) {
        recordError();
    }

    @Override
    public void recordException(boolean markError, Throwable throwable) {

    }


    @Override
    public void recordApiId(int apiId) {

    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor) {

    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {

    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {

    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {

    }

    @Override
    public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, String value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, int value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, Object value) {

    }

    @Override
    public void recordServiceType(ServiceType serviceType) {

    }

    @Override
    public void recordRpcName(String rpc) {
        traceRoot.getShared().setRpcName(rpc);
    }

    @Override
    public void recordRemoteAddress(String remoteAddress) {

    }

    @Override
    public void recordEndPoint(String endPoint) {
        traceRoot.getShared().setEndPoint(endPoint);
    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {

    }

    @Override
    public void recordAcceptorHost(String host) {

    }

    @Override
    public void recordLogging(LoggingInfo loggingInfo) {
        traceRoot.getShared().setLoggingInfo(loggingInfo.getCode());
    }

    @Override
    public void recordStatusCode(int statusCode) {
        traceRoot.getShared().setStatusCode(statusCode);
    }

    @Override
    public Object attachFrameObject(Object frameObject) {
        return null;
    }

    @Override
    public Object getFrameObject() {
        return null;
    }

    @Override
    public Object detachFrameObject() {
        return null;
    }
}
