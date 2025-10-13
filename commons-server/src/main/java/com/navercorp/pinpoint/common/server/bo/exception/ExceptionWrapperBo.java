/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.bo.exception;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ExceptionWrapperBo {
    @NonNull
    private final String exceptionClassName;
    @NonNull
    private final String exceptionMessage;
    private final long startTime;
    private final long exceptionId;
    private final int exceptionDepth;

    private final List<StackTraceElementWrapperBo> stackTraceElements;

    public ExceptionWrapperBo(
            String exceptionClassName,
            String exceptionMessage,
            long startTime,
            long exceptionId,
            int exceptionDepth,
            List<StackTraceElementWrapperBo> stackTraceElements
    ) {
        this.exceptionClassName = StringPrecondition.requireHasLength(exceptionClassName, "exceptionClassName");
        this.exceptionMessage = StringPrecondition.requireHasLength(exceptionMessage, "exceptionMessage");
        this.startTime = startTime;
        this.exceptionId = exceptionId;
        this.exceptionDepth = exceptionDepth;
        this.stackTraceElements = Objects.requireNonNull(stackTraceElements, "stackTraceElements");
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getExceptionId() {
        return exceptionId;
    }

    public int getExceptionDepth() {
        return exceptionDepth;
    }

    public List<StackTraceElementWrapperBo> getStackTraceElements() {
        return stackTraceElements;
    }

    @Override
    public String toString() {
        return "ExceptionWrapperBo{" +
                "exceptionClassName='" + exceptionClassName + '\'' +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", startTime=" + startTime +
                ", exceptionId=" + exceptionId +
                ", exceptionDepth=" + exceptionDepth +
                ", stackTraceElements=" + stackTraceElements +
                '}';
    }
}
