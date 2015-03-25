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

import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.util.ParsingResult;


/**
 * @author emeroad
 */
public class DisableTrace  implements Trace {

    public static final DisableTrace INSTANCE = new DisableTrace();
    
    private final Map<String, Object> attributeMap = new HashMap<String, Object>();

    private DisableTrace() {
    }

    @Override
    public void traceBlockBegin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void markBeforeTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getBeforeTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void markAfterTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getAfterTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceBlockBegin(int stackId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceRootBlockEnd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceBlockEnd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceBlockEnd(int stackId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TraceId getTraceId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSampled() {
        // always return false
        return false;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public void recordException(Throwable throwable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParsingResult recordSqlInfo(String sql) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult) {
        throw new UnsupportedOperationException();
    }

    public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordAttribute(AnnotationKey key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordAttribute(AnnotationKey key, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordAttribute(AnnotationKey key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordServiceType(ServiceType serviceType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordRpcName(String rpc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordDestinationId(String destinationId) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void recordEndPoint(String endPoint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordRemoteAddress(String remoteAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordNextSpanId(long spanId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordAcceptorHost(String host) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStackFrameId() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public short getServiceType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String key) {
        return attributeMap.get(key);
    }

    @Override
    public Object setAttribute(String key, Object value) {
        return attributeMap.put(key, value);
    }

    @Override
    public Object removeAttribute(String key) {
        return attributeMap.remove(key);
    }
    
    @Override
    public Object setTraceBlockAttachment(Object attachment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getTraceBlockAttachment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object removeTraceBlockAttachment() {
        throw new UnsupportedOperationException();
    }
}
