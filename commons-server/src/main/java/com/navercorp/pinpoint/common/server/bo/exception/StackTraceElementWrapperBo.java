/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.bo.exception;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class StackTraceElementWrapperBo {
    @NotNull private String className;
    @NotNull private String fileName;
    private int lineNumber;
    @NotNull private String methodName;

    public StackTraceElementWrapperBo(String className,
                                      String fileName,
                                      int lineNumber,
                                      String methodName) {
        this.className = Objects.requireNonNull(className, "className");
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.lineNumber = lineNumber;
        this.methodName = Objects.requireNonNull(methodName, "methodName");
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "StackTraceElementWrapperBo{" +
                "className='" + className + '\'' +
                ", fileName='" + fileName + '\'' +
                ", lineNumber=" + lineNumber +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
