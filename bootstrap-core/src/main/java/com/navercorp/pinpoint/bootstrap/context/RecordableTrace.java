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

package com.navercorp.pinpoint.bootstrap.context;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author emeroad
 */
public interface RecordableTrace {

    void markBeforeTime();

    long getBeforeTime();

    void markAfterTime();

    long getAfterTime();

    TraceId getTraceId();
    
    AsyncTraceId getAsyncTraceId();

    boolean canSampled();

    boolean isRoot();
    
    short getServiceType();

    void recordException(Throwable throwable);

    void recordApi(MethodDescriptor methodDescriptor);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args);

    void recordApi(MethodDescriptor methodDescriptor, Object args, int index);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end);

    void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index);

    ParsingResult recordSqlInfo(String sql);

    void recordSqlParsingResult(ParsingResult parsingResult);

    void recordSqlParsingResult(ParsingResult parsingResult, String bindValue);

    void recordAttribute(AnnotationKey key, String value);

    void recordAttribute(AnnotationKey key, int value);

    void recordAttribute(AnnotationKey key, Object value);

    void recordServiceType(ServiceType serviceType);

    void recordRpcName(String rpc);

    void recordDestinationId(String destinationId);

    void recordEndPoint(String endPoint);

    void recordRemoteAddress(String remoteAddress);

    void recordNextSpanId(long spanId);

    void recordParentApplication(String parentApplicationName, short parentApplicationType);
    
    void recordLogging(boolean isLogging);

    /**
     * 
     * when WAS_A sends a request to WAS_B, WAS_A stores its own data through parameters sent by WAS_B.
     * This data is needed to extract caller/callee relationship.
     * 
     * @param host (we need to extract hostname from the request URL)
     * 
     *
     */
    void recordAcceptorHost(String host);

    int getStackFrameId();
    
    void recordAsyncId(int asyncId);
    
    void recordNextAsyncId(int asyncId);
    
    void recordAsyncSequence(short sequence);
    
    boolean isAsync();
    
    long getTraceStartTime(); 
}