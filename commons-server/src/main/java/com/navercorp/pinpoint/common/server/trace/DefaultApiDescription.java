/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.server.util.ParameterUtils;
import com.navercorp.pinpoint.common.util.ClassUtils;

import java.util.Arrays;
import java.util.Objects;

public class DefaultApiDescription implements ApiDescription {
    private final String apiDescription;

    private final String className;

    private final String methodName;

    private final String[] simpleParameter;

    private final int line;

    public DefaultApiDescription(String apiDescription, String className, String methodName, String[] simpleParameter, int line) {
        this.apiDescription = Objects.requireNonNull(apiDescription, "apiDescription");
        this.className = Objects.requireNonNull(className, "className");
        this.methodName = Objects.requireNonNull(methodName, "methodName");
        this.simpleParameter = simpleParameter;
        this.line = line;
    }

    @Override
    public String getApiDescription() {
        return apiDescription;
    }

    @Override
    public String getSimpleClassName() {
        int classNameStartIndex = className.lastIndexOf('.') + 1;
        return className.substring(classNameStartIndex);
    }

    @Override
    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return ClassUtils.getPackageName(className);
    }

    @Override
    public String getMethodName() {
        return this.methodName;
    }

    @Override
    public String[] getSimpleParameter() {
        return simpleParameter;
    }

    @Override
    public String getMethodDescription() {
        String simpleParameterDescription = ParameterUtils.join(simpleParameter, ", ");
        return methodName + simpleParameterDescription;
    }


    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public String toString() {
        return "JavaApiDescription{" +
                "apiDescription='" + apiDescription + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", simpleParameter=" + Arrays.toString(simpleParameter) +
                ", line=" + line +
                '}';
    }
}