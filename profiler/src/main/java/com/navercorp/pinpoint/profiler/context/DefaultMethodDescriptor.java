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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.common.trace.MethodType;
import com.navercorp.pinpoint.profiler.util.ApiUtils;

import java.util.Arrays;

/**
 * @author emeroad
 */
public class DefaultMethodDescriptor implements MethodDescriptor {
    private final String className;

    private final String methodName;

    private final String[] parameterTypes;

    private final String[] parameterVariableName;


    private final String parameterDescriptor;

    private final String apiDescriptor;

    private final int lineNumber;

    private int apiId = 0;

    private String fullName;

    private final int type = MethodType.DEFAULT;


    public DefaultMethodDescriptor(String className, String methodName, String[] parameterTypes, String[] parameterVariableName, int lineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameterVariableName = parameterVariableName;
        this.parameterDescriptor = ApiUtils.mergeParameterVariableNameDescription(parameterTypes, parameterVariableName);
        this.apiDescriptor = ApiUtils.mergeApiDescriptor(className, methodName, parameterDescriptor);
        this.lineNumber = lineNumber;
    }

    public String getParameterDescriptor() {
        return parameterDescriptor;
    }


    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getClassName() {
        return className;
    }


    @Override
    public String[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public String[] getParameterVariableName() {
        return parameterVariableName;
    }


    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getFullName() {
        if (fullName != null) {
            return fullName;
        }
        StringBuilder buffer = new StringBuilder(256);
        buffer.append(className);
        buffer.append('.');
        buffer.append(methodName);
        buffer.append(parameterDescriptor);
        if (lineNumber != -1) {
            buffer.append(':');
            buffer.append(lineNumber);
        }
        fullName = buffer.toString();
        return fullName;
    }


    @Override
    public String getApiDescriptor() {
        return apiDescriptor;
    }

    @Override
    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    @Override
    public int getApiId() {
        return apiId;
    }

    public int getType() {
        return type;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{className=");
        builder.append(className);
        builder.append(", methodName=");
        builder.append(methodName);
        builder.append(", parameterTypes=");
        builder.append(Arrays.toString(parameterTypes));
        builder.append(", parameterVariableName=");
        builder.append(Arrays.toString(parameterVariableName));
        builder.append(", parameterDescriptor=");
        builder.append(parameterDescriptor);
        builder.append(", apiDescriptor=");
        builder.append(apiDescriptor);
        builder.append(", lineNumber=");
        builder.append(lineNumber);
        builder.append(", apiId=");
        builder.append(apiId);
        builder.append(", fullName=");
        builder.append(fullName);
        builder.append(", type=");
        builder.append(type);
        builder.append("}");
        return builder.toString();
    }
}
