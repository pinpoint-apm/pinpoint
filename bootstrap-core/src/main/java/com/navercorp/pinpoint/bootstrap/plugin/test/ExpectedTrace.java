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
 */
public class ExpectedTrace {
    private final TraceType type;
    private final String serviceType;
    private final Member method;
    private final String methodSignature;
    private final Exception exception;
    private final ExpectedTraceField rpc;
    private final ExpectedTraceField endPoint;
    private final ExpectedTraceField remoteAddr;
    private final ExpectedTraceField destinationId;
    private final ExpectedAnnotation[] annotations;
    private final ExpectedTrace[] asyncTraces;

    private ExpectedTrace(Builder builder) {
        this.type = builder.type;
        this.serviceType = builder.serviceType;
        this.method = builder.method;
        this.methodSignature = builder.methodSignature;
        this.exception = builder.exception;
        this.rpc = builder.rpc;
        this.endPoint = builder.endPoint;
        this.remoteAddr = builder.remoteAddr;
        this.destinationId = builder.destinationId;
        this.annotations = builder.annotations;
        this.asyncTraces = builder.asyncTraces;
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

    public Exception getException() {
        return exception;
    }

    public ExpectedTraceField getRpc() {
        return rpc;
    }

    public ExpectedTraceField getEndPoint() {
        return endPoint;
    }

    public ExpectedTraceField getRemoteAddr() {
        return remoteAddr;
    }

    public ExpectedTraceField getDestinationId() {
        return destinationId;
    }

    public ExpectedAnnotation[] getAnnotations() {
        return annotations;
    }

    public ExpectedTrace[] getAsyncTraces() {
        return asyncTraces;
    }


    public static Builder createBuilder(TraceType traceType, String serviceType) {
        return new Builder(traceType, serviceType);
    }

    public static Builder createRootBuilder(String serviceType) {
        return new Builder(TraceType.ROOT, serviceType);
    }

    public static Builder createEventBuilder(String serviceType) {
        return new Builder(TraceType.EVENT, serviceType);
    }

    public static class Builder {

        private final TraceType type;
        private final String serviceType;

        private Member method;
        private String methodSignature;
        private Exception exception;
        private ExpectedTraceField rpc = ExpectedTraceField.ALWAYS_TRUE;
        private ExpectedTraceField endPoint = ExpectedTraceField.ALWAYS_TRUE;
        private ExpectedTraceField remoteAddr = ExpectedTraceField.ALWAYS_TRUE;
        private ExpectedTraceField destinationId = ExpectedTraceField.ALWAYS_TRUE;
        private ExpectedAnnotation[] annotations;
        private ExpectedTrace[] asyncTraces;

        public Builder(TraceType type, String serviceType) {
            if (type == null) {
                throw new NullPointerException("type");
            }
            if (serviceType == null) {
                throw new NullPointerException("serviceType");
            }
            this.type = type;
            this.serviceType = serviceType;
        }

        public void setMethod(Member method) {
            this.method = method;
        }

        public void setMethodSignature(String methodSignature) {
            this.methodSignature = methodSignature;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public void setRpc(String rpc) {
            setRpc(ExpectedTraceField.create(rpc));
        }

        public void setRpc(ExpectedTraceField rpc) {
            if (rpc == null) {
                throw new NullPointerException("rpc");
            }
            this.rpc = rpc;
        }

        public void setEndPoint(String endPoint) {
            setEndPoint(ExpectedTraceField.create(endPoint));
        }

        public void setEndPoint(ExpectedTraceField endPoint) {
            if (endPoint == null) {
                throw new NullPointerException("endPoint");
            }
            this.endPoint = endPoint;
        }

        public void setRemoteAddr(String remoteAddr) {
            setRemoteAddr(ExpectedTraceField.create(remoteAddr));
        }

        public void setRemoteAddr(ExpectedTraceField remoteAddr) {
            if (remoteAddr == null) {
                throw new NullPointerException("remoteAddr");
            }
            this.remoteAddr = remoteAddr;
        }

        public void setDestinationId(String destinationId) {
            setDestinationId(ExpectedTraceField.create(destinationId));
        }

        public void setDestinationId(ExpectedTraceField destinationId) {
            if (destinationId == null) {
                throw new NullPointerException("destinationId");
            }
            this.destinationId = destinationId;
        }

        public void setAnnotations(ExpectedAnnotation... annotations) {
            this.annotations = annotations;
        }

        public void setAsyncTraces(ExpectedTrace[] asyncTraces) {
            this.asyncTraces = asyncTraces;
        }

        public ExpectedTrace build() {
            return new ExpectedTrace(this);
        }

    }

}
