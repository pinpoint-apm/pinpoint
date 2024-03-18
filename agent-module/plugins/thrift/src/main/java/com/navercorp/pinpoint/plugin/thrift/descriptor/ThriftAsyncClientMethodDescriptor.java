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

package com.navercorp.pinpoint.plugin.thrift.descriptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.common.trace.MethodType;
import com.navercorp.pinpoint.common.util.LineNumber;

/**
 * @author HyunGil Jeong
 */
public class ThriftAsyncClientMethodDescriptor implements MethodDescriptor {
    
    private int apiId = 0;

    @Override
    public String getMethodName() {
        return "";
    }

    @Override
    public String getClassName() {
        return "";
    }

    @Override
    public String[] getParameterTypes() {
        return null;
    }

    @Override
    public String[] getParameterVariableName() {
        return null;
    }

    @Override
    public String getParameterDescriptor() {
        return "()";
    }

    @Override
    public int getLineNumber() {
        return LineNumber.NO_LINE_NUMBER;
    }

    @Override
    public String getFullName() {
        return ThriftAsyncClientMethodDescriptor.class.getName();
    }

    @Override
    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    @Override
    public int getApiId() {
        return this.apiId;
    }

    @Override
    public String getApiDescriptor() {
        return "Thrift Asynchronous Client Invocation";
    }

    @Override
    public int getType() {
        return MethodType.INVOCATION;
    }

}
