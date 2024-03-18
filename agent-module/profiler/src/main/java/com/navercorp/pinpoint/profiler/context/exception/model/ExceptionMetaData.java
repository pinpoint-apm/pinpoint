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

import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;

import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ExceptionMetaData implements MetaDataType {

    private final List<ExceptionWrapper> exceptionWrappers;

    private final TraceRoot traceRoot;

    public ExceptionMetaData(List<ExceptionWrapper> exceptionWrappers, TraceRoot traceRoot) {
        this.exceptionWrappers = Objects.requireNonNull(exceptionWrappers);
        this.traceRoot = Objects.requireNonNull(traceRoot);
    }

    public List<ExceptionWrapper> getExceptionWrappers() {
        return exceptionWrappers;
    }

    public TraceRoot getTraceRoot() {
        return traceRoot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExceptionMetaData)) return false;

        ExceptionMetaData that = (ExceptionMetaData) o;

        return Objects.equals(exceptionWrappers, that.exceptionWrappers);
    }

    @Override
    public int hashCode() {
        return exceptionWrappers != null ? exceptionWrappers.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ExceptionMetaData{" +
                "exceptionWrappers=" + exceptionWrappers +
                ", traceRoot=" + traceRoot +
                '}';
    }
}
