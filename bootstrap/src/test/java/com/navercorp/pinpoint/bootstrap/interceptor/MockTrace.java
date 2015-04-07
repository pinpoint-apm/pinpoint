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

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.util.Clock;
import com.navercorp.pinpoint.common.util.ParsingResult;
import com.navercorp.pinpoint.common.util.SystemClock;

/**
 * @author emeroad
 */
public class MockTrace implements Trace {

    private long beforeTime;
    private long afterTime;

    private boolean sampled = true;

    private Clock clock = SystemClock.INSTANCE;
        
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void markBeforeTime() {
        beforeTime = clock.getTime();
    }

    @Override
    public long getBeforeTime() {
        return beforeTime;
    }

    @Override
    public void markAfterTime() {
        afterTime = clock.getTime();
    }

    @Override
    public long getAfterTime() {
        return afterTime;
    }

    @Override
    public TraceId getTraceId() {
        return null;
    }

    public void setSampled(boolean sampled) {
        this.sampled = sampled;
    }

    @Override
    public boolean canSampled() {
        return sampled;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public void recordException(Throwable throwable) {

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
    public ParsingResult recordSqlInfo(String sql) {
        return null;
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult) {

    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {

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

    }

    @Override
    public void recordDestinationId(String destinationId) {

    }

    @Override
    public void recordEndPoint(String endPoint) {

    }

    @Override
    public void recordRemoteAddress(String remoteAddress) {

    }

    @Override
    public void recordNextSpanId(long spanId) {

    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {

    }

    @Override
    public void recordAcceptorHost(String host) {

    }

    @Override
    public int getStackFrameId() {
        return 0;
    }

    @Override
    public void traceBlockBegin() {

    }

    @Override
    public void traceBlockBegin(int stackId) {

    }

    @Override
    public void traceRootBlockEnd() {

    }

    @Override
    public void traceBlockEnd() {

    }

    @Override
    public void traceBlockEnd(int stackId) {

    }
    
    @Override
    public short getServiceType() {
        return ServiceType.UNDEFINED.getCode();
    }
}
