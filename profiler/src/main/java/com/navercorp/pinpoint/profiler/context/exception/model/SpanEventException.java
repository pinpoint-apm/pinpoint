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

import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class SpanEventException {

    private final List<ExceptionWrapper> exceptionWrappers;

    private final long startTime;

    private final long exceptionId;

    public SpanEventException(List<ExceptionWrapper> exceptionWrappers, long startTime, long exceptionId) {
        this.exceptionWrappers = exceptionWrappers;
        this.startTime = startTime;
        this.exceptionId = exceptionId;
    }

    public long getStartTime() {
        return startTime;
    }

    public List<ExceptionWrapper> getExceptionWrappers() {
        return exceptionWrappers;
    }

    public long getExceptionId() {
        return exceptionId;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        SpanEventException that = (SpanEventException) o;
//
//        if (startTime != that.startTime) return false;
//        if (exceptionId != that.exceptionId) return false;
//        // Probably incorrect - comparing Object[] arrays with Arrays.equals
//        return Arrays.equals(exceptionWrappers, that.exceptionWrappers);
//    }
//
//    @Override
//    public int hashCode() {
//        int result = Arrays.hashCode(exceptionWrappers);
//        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
//        result = 31 * result + (int) (exceptionId ^ (exceptionId >>> 32));
//        return result;
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpanEventException that = (SpanEventException) o;

        if (startTime != that.startTime) return false;
        if (exceptionId != that.exceptionId) return false;
        return Objects.equals(exceptionWrappers, that.exceptionWrappers);
    }

    @Override
    public int hashCode() {
        int result = exceptionWrappers != null ? exceptionWrappers.hashCode() : 0;
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (exceptionId ^ (exceptionId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "SpanEventException{" +
                "exceptionWrappers=" + exceptionWrappers +
                ", startTime=" + startTime +
                ", exceptionId=" + exceptionId +
                '}';
    }
}
