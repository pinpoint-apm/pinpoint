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
import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.profiler.context.errorhandler.IgnoreErrorHandler;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.Shared;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DisableSpanRecorder implements SpanRecorder {

    public static final String UNSUPPORTED_OPERATION = "DisableSpanRecorder";

    private final LocalTraceRoot traceRoot;
    private final IgnoreErrorHandler ignoreErrorHandler;

    public DisableSpanRecorder(LocalTraceRoot traceRoot, IgnoreErrorHandler ignoreErrorHandler) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.ignoreErrorHandler = Objects.requireNonNull(ignoreErrorHandler, "ignoreErrorHandler");
    }

    @Override
    public boolean canSampled() {
        return false;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public void recordStartTime(long startTime) {

    }

    @Override
    public void recordTime(boolean autoTimeRecoding) {

    }

    @Override
    public void recordError() {
        getShared().maskErrorCode(1);
    }

    @Override
    public void recordException(Throwable throwable) {
        recordException(true, throwable);
    }

    @Override
    public void recordException(boolean markError, Throwable throwable) {
        if (markError) {
            if (!ignoreErrorHandler.handleError(throwable)) {
                recordError();
            }
        }
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
    public void recordAttribute(AnnotationKey key, Integer value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, long value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, Long value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, Object value) {

    }

    @Override
    public void recordServiceType(ServiceType serviceType) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, boolean value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, double value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, byte[] value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, DataType value) {

    }

    @Override
    public void recordRpcName(String rpc) {
    }

    @Override
    public void recordRemoteAddress(String remoteAddress) {

    }

    @Override
    public void recordEndPoint(String endPoint) {
    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {

    }

    @Override
    public void recordAcceptorHost(String host) {

    }

    @Override
    public void recordLogging(LoggingInfo loggingInfo) {
    }

    @Override
    public void recordStatusCode(int statusCode) {
        getShared().setStatusCode(statusCode);
    }

    @Override
    public boolean recordUriTemplate(String uriTemplate) {
        return recordUriTemplate(uriTemplate, false);
    }


    @Override
    public boolean recordUriTemplate(String uriTemplate, boolean force) {
        return getShared().setUriTemplate(uriTemplate, force);
    }

    private Shared getShared() {
        return traceRoot.getShared();
    }

    @Override
    public Object attachFrameObject(Object frameObject) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public Object getFrameObject() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public Object detachFrameObject() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }
}
