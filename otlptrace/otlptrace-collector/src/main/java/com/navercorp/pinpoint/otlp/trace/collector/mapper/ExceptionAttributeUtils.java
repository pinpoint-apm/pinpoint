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

import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.trace.v1.Span;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Shared helpers for reading the OTel {@code exception} span event and its attributes.
 * Used both by {@link OtlpExceptionMapper} (exception-trace metadata) and by
 * {@link OtlpExceptionInfoResolver#resolveErrorExceptionInfo} (SpanBo/SpanEventBo exceptionInfo),
 * so the exception class-name resolution stays consistent between the two.
 */
public final class ExceptionAttributeUtils {

    private ExceptionAttributeUtils() {
    }

    /**
     * Returns the first {@code exception} event on the span, or {@code null} when absent.
     */
    public static Span.Event findExceptionEvent(Span span) {
        return findEvent(span.getEventsList(), OtlpTraceConstants.EVENT_NAME_EXCEPTION);
    }

    /**
     * Returns the first event whose name equals {@code name}, or {@code null} when none match.
     */
    public static Span.Event findEvent(List<Span.Event> events, String name) {
        Objects.requireNonNull(name, "name");
        for (Span.Event event : events) {
            if (name.equals(event.getName())) {
                return event;
            }
        }
        return null;
    }

    /**
     * Resolves the exception class name, preferring the event's {@code exception.type} and
     * falling back to the span attribute {@code error.type}. Returns {@code null} when neither
     * is present.
     */
    public static String resolveExceptionType(Map<String, AttributeValue> eventAttributes,
                                              Map<String, AttributeValue> spanAttributes) {
        final String eventType = AttributeUtils.getAttributeStringValue(eventAttributes, OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_TYPE, null);
        if (StringUtils.hasLength(eventType)) {
            return eventType;
        }
        return AttributeUtils.getAttributeStringValue(spanAttributes, OtlpTraceConstants.ATTRIBUTE_KEY_ERROR_TYPE, null);
    }

    /**
     * Returns the event's {@code exception.message}, or {@code null} when absent.
     */
    public static String getExceptionMessage(Map<String, AttributeValue> eventAttributes) {
        return AttributeUtils.getAttributeStringValue(eventAttributes, OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_MESSAGE, null);
    }
}
