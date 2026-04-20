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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionWrapperBo;
import com.navercorp.pinpoint.common.server.bo.exception.StackTraceElementWrapperBo;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants.ATTRIBUTE_KEY_ERROR_TYPE;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_MESSAGE;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_STACKTRACE;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_TYPE;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants.EVENT_NAME_EXCEPTION;

@Component
public class OtlpExceptionMapper {

    private static final String EMPTY = "";

    public Optional<ExceptionMetaDataBo> map(IdAndName idAndName, Span span) {
        if (Status.StatusCode.STATUS_CODE_ERROR.getNumber() != span.getStatus().getCodeValue()) {
            return Optional.empty();
        }

        Span.Event exceptionEvent = findExceptionEvent(span);
        if (exceptionEvent == null) {
            return Optional.empty();
        }

        final Map<String, Object> eventAttrs = OtlpTraceMapperUtils.getAttributeToMap(exceptionEvent.getAttributesList());
        String exceptionType = AttributeUtils.getStringValue(eventAttrs, ATTRIBUTE_KEY_EXCEPTION_TYPE, null);
        if (exceptionType == null) {
            // fallback: span attribute의 error.type
            final Map<String, Object> spanAttrs = OtlpTraceMapperUtils.getAttributeToMap(span.getAttributesList());
            exceptionType = AttributeUtils.getStringValue(spanAttrs, ATTRIBUTE_KEY_ERROR_TYPE, null);
        }
        if (!StringUtils.hasLength(exceptionType)) {
            return Optional.empty();
        }

        final String exceptionMessage = AttributeUtils.getStringValue(eventAttrs, ATTRIBUTE_KEY_EXCEPTION_MESSAGE, EMPTY);
        final String stackTraceStr = AttributeUtils.getStringValue(eventAttrs, ATTRIBUTE_KEY_EXCEPTION_STACKTRACE, EMPTY);

        final long eventTime = exceptionEvent.getTimeUnixNano() > 0
                ? TimeUnit.NANOSECONDS.toMillis(exceptionEvent.getTimeUnixNano())
                : TimeUnit.NANOSECONDS.toMillis(span.getStartTimeUnixNano());

        final long spanId = OtlpTraceMapperUtils.getSpanId(span.getSpanId());
        final List<StackTraceElementWrapperBo> stackTrace = parseStackTrace(stackTraceStr);

        ExceptionWrapperBo wrapper = new ExceptionWrapperBo(
                exceptionType,
                exceptionMessage,
                eventTime,
                spanId,
                0,
                stackTrace
        );

        final OtelServerTraceId transactionId = new OtelServerTraceId(span.getTraceId().toByteArray());
        ExceptionMetaDataBo bo = new ExceptionMetaDataBo(
                transactionId,
                spanId,
                ServiceType.OPENTELEMETRY_SERVER.getCode(),
                idAndName.applicationName(),
                idAndName.agentId(),
                span.getName()
        );
        bo.setExceptionWrapperBos(List.of(wrapper));
        return Optional.of(bo);
    }

    private Span.Event findExceptionEvent(Span span) {
        for (Span.Event event : span.getEventsList()) {
            if (EVENT_NAME_EXCEPTION.equals(event.getName())) {
                return event;
            }
        }
        return null;
    }

    List<StackTraceElementWrapperBo> parseStackTrace(String stackTrace) {
        List<StackTraceElementWrapperBo> result = new ArrayList<>();
        if (!StringUtils.hasLength(stackTrace)) {
            return result;
        }

        for (String line : stackTrace.split("\n")) {
            final String trimmed = line.trim();
            if (!trimmed.startsWith("at ")) {
                continue;
            }

            final String element = trimmed.substring(3);
            final int parenOpen = element.lastIndexOf('(');
            final int parenClose = element.lastIndexOf(')');
            if (parenOpen < 0 || parenClose <= parenOpen) {
                continue;
            }

            final String methodSignature = element.substring(0, parenOpen);
            final String fileInfo = element.substring(parenOpen + 1, parenClose);

            final int lastDot = methodSignature.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }

            final String className = methodSignature.substring(0, lastDot);
            final String methodName = methodSignature.substring(lastDot + 1);
            if (!StringUtils.hasLength(className) || !StringUtils.hasLength(methodName)) {
                continue;
            }

            final String fileName;
            final int lineNumber;
            final int colonIdx = fileInfo.lastIndexOf(':');
            if (colonIdx >= 0) {
                fileName = fileInfo.substring(0, colonIdx);
                int parsed;
                try {
                    parsed = Integer.parseInt(fileInfo.substring(colonIdx + 1));
                } catch (NumberFormatException e) {
                    parsed = 0;
                }
                lineNumber = parsed;
            } else {
                fileName = fileInfo;
                lineNumber = "Native Method".equals(fileInfo) ? -2 : -1;
            }

            result.add(new StackTraceElementWrapperBo(className, fileName, lineNumber, methodName));
        }
        return result;
    }
}
