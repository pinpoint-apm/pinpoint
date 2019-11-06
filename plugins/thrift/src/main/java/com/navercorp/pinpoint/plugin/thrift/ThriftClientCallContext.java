/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift;


/**
 * @author HyunGil Jeong
 */
public class ThriftClientCallContext {
    
    public static final ThriftHeader NONE = null;
    
    private final String methodName;

    private boolean isEntryPoint;
    
    private ThriftHeader traceHeaderToBeRead;
    
    private ThriftRequestProperty traceHeader;
    
    public ThriftClientCallContext(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isEntryPoint() {
        return isEntryPoint;
    }

    public void setEntryPoint(boolean entryPoint) {
        isEntryPoint = entryPoint;
    }

    public ThriftHeader getTraceHeaderToBeRead() {
        return traceHeaderToBeRead;
    }

    public void setTraceHeaderToBeRead(ThriftHeader traceHeaderToBeRead) {
        this.traceHeaderToBeRead = traceHeaderToBeRead;
    }

    public ThriftRequestProperty getTraceHeader() {
        return traceHeader;
    }

    public void setTraceHeader(ThriftRequestProperty traceHeader) {
        this.traceHeader = traceHeader;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThriftClientCallContext{");
        sb.append("methodName='").append(methodName).append('\'');
        sb.append(", isEntryPoint=").append(isEntryPoint);
        sb.append(", traceHeaderToBeRead=").append(traceHeaderToBeRead);
        sb.append(", traceHeader=").append(traceHeader);
        sb.append('}');
        return sb.toString();
    }
}
