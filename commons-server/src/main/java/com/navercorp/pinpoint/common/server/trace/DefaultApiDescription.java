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

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.ClassUtils;

public class DefaultApiDescription implements ApiDescription {
    private String className;

    private String methodName;

    private String[] simpleParameter;

    private int line = -1;

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String getSimpleClassName() {
        int classNameStartIndex = className.lastIndexOf('.') + 1;
        return className.substring(classNameStartIndex, className.length());
    }

    public String getPackageNameName() {
        return ClassUtils.getPackageName(className);
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setSimpleParameter(String[] simpleParameter) {
        this.simpleParameter = simpleParameter;
    }

    public String[] getSimpleParameter() {
        return simpleParameter;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getSimpleMethodDescription() {
        String simpleParameterDescription = concateLine(simpleParameter, ", ");
        return methodName + simpleParameterDescription;
    }

    public String concateLine(String[] stringList, String separator) {
        if (ArrayUtils.isEmpty(stringList)) {
            return "()";
        }

        StringBuilder sb = new StringBuilder();
        if (stringList.length > 0) {
            sb.append('(');
            sb.append(stringList[0]);
            for (int i = 1; i < stringList.length; i++) {
                sb.append(separator);
                sb.append(stringList[i]);
            }
            sb.append(')');
        }
        return sb.toString();
    }
}