/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.plugin.test;

import java.lang.reflect.Member;

/**
 * @author Jongho Moon
 *
 */
public class ExpectedTrace {
    private final TraceType type;
    private final String serviceType;
    private final Member method;
    private final String methodSignature;
    private final String rpc;
    private final String endPoint;
    private final String remoteAddr;
    private final String destinationId;
    private final ExpectedAnnotation[] annotations;
    private final ExpectedTrace[] asyncTraces;
    
    public ExpectedTrace(TraceType type, String serviceType, Member method, String methodSignature, String rpc, String endPoint, String remoteAddr, String destinationId, ExpectedAnnotation[] annotations, ExpectedTrace[] asyncTraces) {
        this.type = type;
        this.serviceType = serviceType;
        this.method = method;
        this.methodSignature = methodSignature;
        this.rpc = rpc;
        this.endPoint = endPoint;
        this.remoteAddr = remoteAddr;
        this.destinationId = destinationId;
        this.annotations = annotations;
        this.asyncTraces = asyncTraces;
    }

    public TraceType getType() {
        return type;
    }

    public String getServiceType() {
        return serviceType;
    }

    public Member getMethod() {
        return method;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public String getRpc() {
        return rpc;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public ExpectedAnnotation[] getAnnotations() {
        return annotations;
    }

    public ExpectedTrace[] getAsyncTraces() {
        return asyncTraces;
    }
}
