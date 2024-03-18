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
package com.navercorp.pinpoint.profiler.context.exception.model;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ExceptionWrapper {
    private static final String EMPTY_STRING = "";
    private final String exceptionClassName;
    private final String exceptionMessage;
    private final StackTraceElement[] stackTraceElements;

    private final long startTime;
    private final long exceptionId;
    private final int exceptionDepth;

    public ExceptionWrapper(
            String exceptionClassName,
            String exceptionMessage,
            StackTraceElement[] stackTraceElements,
            long startTime,
            long exceptionId,
            int exceptionDepth
    ) {
        this.exceptionClassName = Objects.requireNonNull(exceptionClassName, "exceptionClassName");
        this.exceptionMessage = Objects.requireNonNull(exceptionMessage, "exceptionMessage");
        this.stackTraceElements = Objects.requireNonNull(stackTraceElements, "stackTraceElements");
        this.startTime = startTime;
        this.exceptionId = exceptionId;
        this.exceptionDepth = exceptionDepth;
    }

    public static ExceptionWrapper newException(
            Throwable throwable,
            long startTime, long exceptionId, int exceptionDepth,
            int maxErrorMessageLength
    ) {
        if (throwable == null) {
            return null;
        }
        return new ExceptionWrapper(
                StringUtils.defaultIfEmpty(throwable.getClass().getName(), EMPTY_STRING),
                StringUtils.abbreviate(throwable.getMessage(), maxErrorMessageLength),
                throwable.getStackTrace(),
                startTime,
                exceptionId,
                exceptionDepth
        );
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public StackTraceElement[] getStackTraceElements() {
        return stackTraceElements;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExceptionWrapper)) return false;

        ExceptionWrapper that = (ExceptionWrapper) o;

        if (startTime != that.startTime) return false;
        if (exceptionId != that.exceptionId) return false;
        if (exceptionDepth != that.exceptionDepth) return false;
        if (!exceptionClassName.equals(that.exceptionClassName)) return false;
        if (!exceptionMessage.equals(that.exceptionMessage)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(stackTraceElements, that.stackTraceElements);
    }

    @Override
    public int hashCode() {
        int result = exceptionClassName.hashCode();
        result = 31 * result + exceptionMessage.hashCode();
        result = 31 * result + Arrays.hashCode(stackTraceElements);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (exceptionId ^ (exceptionId >>> 32));
        result = 31 * result + exceptionDepth;
        return result;
    }

    @Override
    public String toString() {
        return "ExceptionWrapper{" +
                "exceptionClassName='" + exceptionClassName + '\'' +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", stackTraceElements=" + Arrays.toString(stackTraceElements) +
                ", startTime=" + startTime +
                ", exceptionId=" + exceptionId +
                ", exceptionDepth=" + exceptionDepth +
                '}';
    }
}
