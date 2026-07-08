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

package com.navercorp.pinpoint.web.trace.callstacks;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.trace.span.Align;

import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class ExceptionRecord extends BaseRecord {

    // Fallback title for an OTel error that carries no exception class name (status ERROR
    // without an exception event). "ERROR" is OpenTelemetry's language-agnostic term for a
    // general failure, unlike the language-specific "Exception".
    static final String OTEL_UNKNOWN_EXCEPTION_TITLE = "ERROR";

    public ExceptionRecord(
            final int tab, final int id, final int parentId, final Align align,
            ServiceType applicationServiceType
    ) {
        this.begin = align.getStartTimeMillis();
        this.beginTimeNanos = align.getStartTimeNanos();
        this.endTimeNanos = align.getEndTimeNanos();
        this.elapsed = align.getElapsedMillis();
        this.tab = tab;
        this.id = id;
        this.parentId = parentId;
        final String simpleName = toSimpleExceptionName(align.getExceptionClass());
        this.title = (align.isOpenTelemetry() && simpleName.isEmpty())
                ? OTEL_UNKNOWN_EXCEPTION_TITLE
                : simpleName;
        this.arguments = buildArgument(align);
        this.isAuthorized = true;
        this.hasException = !align.isSpan();
        this.agentId = align.getAgentId();
        this.applicationName = align.getApplicationName();
        this.serviceName = align.getServiceName();
        this.applicationServiceType = applicationServiceType;
        this.exceptionChainId = toExceptionChainId(align.getAnnotationBoList());
    }

    String toSimpleExceptionName(String exceptionClass) {
        if (exceptionClass == null) {
            return "";
        }
        final int index = exceptionClass.lastIndexOf('.');
        if (index != -1) {
            exceptionClass = exceptionClass.substring(index + 1);
        }
        return exceptionClass;
    }

    long toExceptionChainId(List<AnnotationBo> annotationBoList) {
        for (AnnotationBo annotationBo : annotationBoList) {
            if (annotationBo.getKey() == AnnotationKey.EXCEPTION_CHAIN_ID.getCode()
                    && annotationBo.getValue() instanceof Long longValue) {
                return longValue;
            }
        }
        return -1;
    }

    String buildArgument(Align align) {
        final ExceptionInfo exceptionInfo = align.getExceptionInfo();
        if (exceptionInfo == null) {
            return "";
        }
        if (align.isOpenTelemetry()) {
            // OTel encodes "<className>:<message>" in exceptionInfo.message; the class name is
            // shown as the title, so the argument is only the message part.
            return Objects.toString(ExceptionInfo.otelMessageBody(exceptionInfo.message()), "");
        }
        return Objects.toString(exceptionInfo.message(), "");
    }
}
