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

package com.navercorp.pinpoint.web.trace.span;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface Align {
    boolean isMeta();

    boolean isSpan();

    SpanBo getSpanBo();

    SpanEventBo getSpanEventBo();

    boolean hasChild();

    int getId();

    void setId(int id);

    long getGapMillis();

    void setGapMillis(long gapMillis);

    long getGapNanos();

    void setGapNanos(long gapNanos);

    int getDepth();

    void setDepth(int depth);

    boolean isAsync();

    boolean isAsyncFirst();

    long getExecutionMillis();

    void setExecutionMillis(long executionMillis);

    long getExecutionNanos();

    void setExecutionNanos(long executionNanos);

    long getCollectorAcceptTime();

    byte getLoggingTransactionInfo();

    default long getStartTimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(getStartTimeNanos());
    }

    long getStartTimeNanos();

    default long getEndTimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(getEndTimeNanos());
    }

    long getEndTimeNanos();

    default long getElapsedMillis() {
        return TimeUnit.NANOSECONDS.toMillis(getElapsedNanos());
    }

    long getElapsedNanos();


    String getAgentId();

    String getAgentName();

    String getApplicationName();

    String getServiceName();


    int getApplicationServiceType();

    long getAgentStartTime();

    int getServiceType();

    String getTransactionId();

    long getSpanId();

    boolean hasException();

    ExceptionInfo getExceptionInfo();

    String getExceptionClass();

    void setExceptionClass(String exceptionClass);

    String getRemoteAddr();

    String getRpc();

    int getApiId();

    List<AnnotationBo> getAnnotationBoList();

    void setAnnotationBoList(List<AnnotationBo> annotationBoList);

    List<AttributeBo> getAttributeBoList();

    String getDestinationId();

    int getAsyncId();

    boolean isOpenTelemetry();

    long getOpenTelemetrySpanId();

    long getOpenTelemetryParentSpanId();

    long getOpenTelemetryStartTime();
}
