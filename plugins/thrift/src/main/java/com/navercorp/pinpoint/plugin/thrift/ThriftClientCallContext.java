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

import com.navercorp.pinpoint.plugin.thrift.ThriftHeader.ThriftHeaderKey;

/**
 * @author HyunGil Jeong
 */
public class ThriftClientCallContext {
    
    public static final ThriftHeaderKey NONE = null;
    
    private final String methodName;
    
    private ThriftHeaderKey traceHeaderToBeRead;
    
    private ThriftHeader traceHeader;
    
    public ThriftClientCallContext(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
    
    public ThriftHeaderKey getTraceHeaderToBeRead() {
        return traceHeaderToBeRead;
    }

    public void setTraceHeaderToBeRead(ThriftHeaderKey traceHeaderToBeRead) {
        this.traceHeaderToBeRead = traceHeaderToBeRead;
    }

    public ThriftHeader getTraceHeader() {
        return traceHeader;
    }

    public void setTraceHeader(ThriftHeader traceHeader) {
        this.traceHeader = traceHeader;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ThriftClientCallContext={methodName=").append(this.methodName);
        sb.append(", traceHeaderToBeRead=").append(this.traceHeaderToBeRead.name());
        sb.append(", traceHeader=").append(this.traceHeader.toString());
        sb.append("}");
        return sb.toString();
    }
    
    

}
