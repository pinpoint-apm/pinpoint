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

package com.navercorp.pinpoint.exceptiontrace.common.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.hash.Funnel;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author intr3p1d
 */
@JsonAutoDetect
public class StackTraceElementWrapper {
    private String className;
    private String fileName;
    private int lineNumber;
    private String methodName;

    public StackTraceElementWrapper() {
    }

    public StackTraceElementWrapper(@JsonProperty("className") String className,
                                    @JsonProperty("fileName") String fileName,
                                    @JsonProperty("lineNumber") int lineNumber,
                                    @JsonProperty("methodName") String methodName) {
        this.className = Objects.requireNonNull(className, "className");
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.lineNumber = lineNumber;
        this.methodName = Objects.requireNonNull(methodName, "methodName");
    }

    public static Funnel<StackTraceElementWrapper> funnel() {
        return (wrapper, into) -> into
                .putString(wrapper.className, StandardCharsets.UTF_8)
                .putString(wrapper.fileName, StandardCharsets.UTF_8)
                .putInt(wrapper.lineNumber)
                .putString(wrapper.methodName, StandardCharsets.UTF_8);
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
        return "StackTraceElementWrapper{" +
                "className='" + className + '\'' +
                ", fileName='" + fileName + '\'' +
                ", lineNumber=" + lineNumber +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
