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

import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;

import java.util.Map;

/**
 * Builds the inline SpanBo/SpanEventBo {@link ExceptionInfo} for an OTel span whose status is
 * ERROR. This is the span-field exception summary (class name + message), distinct from
 * {@link OtlpExceptionMapper} which writes the full {@code ExceptionMetaDataBo} to the
 * exception-trace store.
 *
 * <p>Shared by {@link OtlpTraceSpanMapper} (root → SpanBo) and {@link OtlpTraceSpanEventMapper}
 * (non-root → SpanEventBo) so both apply the identical rule.
 */
public final class OtlpExceptionInfoResolver {

    // Upper bound for the exception message body stored inline on SpanBo/SpanEventBo, mirroring
    // the native agent's 256-char abbreviation. The class-name prefix is not counted (it is
    // naturally bounded) and survives truncation because it precedes the body.
    static final int EXCEPTION_MESSAGE_MAX_LENGTH = 256;

    private OtlpExceptionInfoResolver() {
    }

    /**
     * Builds the SpanBo/SpanEventBo {@link ExceptionInfo} for an OTel span whose status is ERROR,
     * or {@code null} when the span is not ERROR (or is ERROR but carries no usable signal).
     *
     * <p>When an {@code exception} event is present, its type ({@code exception.type} →
     * {@code error.type}) is treated as the exception class name; otherwise the free-form status
     * message (with {@code error.type} appended) is used.
     *
     * <p>OTel has no {@code StringMetaData} for the class name, so both the class name and the
     * message are encoded into the single {@link ExceptionInfo#message()} field as
     * {@code "<className><delimiter><message>"} — the class-name prefix is always present (empty
     * when unknown). See {@link ExceptionInfo#OTEL_MESSAGE_DELIMITER}.
     */
    static ExceptionInfo resolveErrorExceptionInfo(Span span, Map<String, AttributeValue> attributes) {
        if (Status.StatusCode.STATUS_CODE_ERROR.getNumber() != span.getStatus().getCodeValue()) {
            return null;
        }

        final Span.Event exceptionEvent = ExceptionAttributeUtils.findExceptionEvent(span);
        if (exceptionEvent != null) {
            final Map<String, AttributeValue> eventAttrs = OtlpTraceMapperUtils.getAttributeValueMap(exceptionEvent.getAttributesList());
            final String className = ExceptionAttributeUtils.resolveExceptionType(eventAttrs, attributes);
            final String message = ExceptionAttributeUtils.getExceptionMessage(eventAttrs);
            if (StringUtils.hasLength(className)) {
                return buildOtelExceptionInfo(className, message);
            }
            // OTel allows an exception event with only exception.message (no exception.type /
            // error.type). Keep that message rather than dropping it and falling back to the
            // (often empty) status message — the empty class-name prefix lets the web side render
            // it under the "ERROR" fallback title.
            if (StringUtils.hasLength(message)) {
                return buildOtelExceptionInfo("", message);
            }
        }

        // No exception event (or unresolved type): use the free-form message. This is a plain
        // message, so the class-name prefix stays empty.
        final String errorType = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_ERROR_TYPE, null);
        final String messageBody = buildExceptionMessageBody(span.getStatus().getMessage(), errorType);
        if (!StringUtils.hasLength(messageBody)) {
            return null;
        }
        return buildOtelExceptionInfo("", messageBody);
    }

    /**
     * Combines the OTel status message and {@code error.type} into a single message body,
     * status message first: {@code "status (error.type)"} when both are present, otherwise
     * whichever is present, or {@code null} when neither is.
     */
    static String buildExceptionMessageBody(String statusMessage, String errorType) {
        final boolean hasMessage = StringUtils.hasLength(statusMessage);
        final boolean hasType = StringUtils.hasLength(errorType);
        if (hasMessage && hasType) {
            return statusMessage + " (" + errorType + ")";
        }
        if (hasMessage) {
            return statusMessage;
        }
        if (hasType) {
            return errorType;
        }
        return null;
    }

    private static ExceptionInfo buildOtelExceptionInfo(String className, String messageBody) {
        final String body = (messageBody == null) ? "" : StringUtils.abbreviate(messageBody, EXCEPTION_MESSAGE_MAX_LENGTH);
        final String message = className + ExceptionInfo.OTEL_MESSAGE_DELIMITER + body;
        return new ExceptionInfo(ExceptionInfo.OTEL_EXCEPTION_ID, message);
    }

    /**
     * True when {@code exceptionInfo} carries a non-empty exception class name prefix — i.e. the
     * span's {@code exception} event was captured. The exception event annotation is then skipped
     * to avoid duplicating what exceptionInfo + exception-trace metadata already hold.
     */
    static boolean isExceptionClassCaptured(ExceptionInfo exceptionInfo) {
        if (exceptionInfo == null || exceptionInfo.message() == null) {
            return false;
        }
        // className is the prefix before the first delimiter; index > 0 ⇒ non-empty class name.
        return exceptionInfo.message().indexOf(ExceptionInfo.OTEL_MESSAGE_DELIMITER) > 0;
    }
}