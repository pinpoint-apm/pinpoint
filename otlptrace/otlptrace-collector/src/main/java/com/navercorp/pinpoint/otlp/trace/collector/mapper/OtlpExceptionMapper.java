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
import com.navercorp.pinpoint.common.server.util.Utf8;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.proto.trace.v1.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_MESSAGE;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_STACKTRACE;

@Component
public class OtlpExceptionMapper {

    private static final String EMPTY = "";

    private static final String TRUNCATED_METRIC = "collector.otlptrace.exception.truncated";

    // Byte-based caps for the client-supplied exception fields (unlike the native path, these arrive
    // as free-form strings from a third-party exporter). Mirrors the module's attribute/SQL/event/link
    // truncation convention (Utf8.truncate) rather than the native char-based abbreviate, since the
    // goal is bounding HBase/Pinot storage size. Truncation is surfaced via a Micrometer counter
    // (tagged by field) — the OPENTELEMETRY_TRUNCATED span annotation does not apply here because the
    // exception is a separate entity written to its own store, not part of the span.
    private final int messageMaxBytes;
    // Max number of parsed stacktrace frames. OTel delivers one flattened stacktrace string, so the
    // unbounded axis is the frame count (not the native exception-chain depth). <= 0 means unlimited.
    private final int stackTraceMaxDepth;
    private final int frameMaxBytes;

    private final Counter messageTruncatedCounter;
    private final Counter stackTraceDepthTruncatedCounter;
    private final Counter frameValueTruncatedCounter;

    public OtlpExceptionMapper(
            @Value("${pinpoint.collector.otlptrace.exception.message-max-bytes:8192}") int messageMaxBytes,
            @Value("${pinpoint.collector.otlptrace.exception.stacktrace.max-depth:256}") int stackTraceMaxDepth,
            @Value("${pinpoint.collector.otlptrace.exception.stacktrace.frame-max-bytes:2048}") int frameMaxBytes,
            MeterRegistry meterRegistry) {
        // Byte caps must be >= 1: a cap of 0 (or negative) would truncate every value to "" and, for
        // the non-empty-constrained className/methodName, make StackTraceElementWrapperBo throw.
        // (stackTraceMaxDepth is intentionally unvalidated — <= 0 is the documented "unlimited".)
        if (messageMaxBytes < 1) {
            throw new IllegalArgumentException("messageMaxBytes must be >= 1: " + messageMaxBytes);
        }
        if (frameMaxBytes < 1) {
            throw new IllegalArgumentException("frameMaxBytes must be >= 1: " + frameMaxBytes);
        }
        this.messageMaxBytes = messageMaxBytes;
        this.stackTraceMaxDepth = stackTraceMaxDepth;
        this.frameMaxBytes = frameMaxBytes;
        this.messageTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "message");
        this.stackTraceDepthTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "stacktrace_depth");
        this.frameValueTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "frame_value");
    }

    /**
     * Maps an OTel 'exception' span event to an {@link ExceptionMetaDataBo}.
     *
     * <p>Recording is gated on the presence of an {@code exception} event (and a resolvable
     * exception type), NOT on the span status: in OTel {@code recordException} and
     * {@code setStatus(ERROR)} are independent, and Pinpoint's native path likewise records
     * exceptions independently of the span error flag.
     *
     * <p>{@code rootSpanId} is the transaction root span id (== the stored root {@code SpanBo}
     * spanId). It is used as {@link ExceptionMetaDataBo}'s spanId so the exception links back to
     * the transaction via {@code (transactionId, spanId)}, mirroring the native agent which maps
     * {@code traceRoot.traceId.spanId}. The exception-bearing span's own id is used as the
     * exceptionId discriminator instead.
     */
    public Optional<ExceptionMetaDataBo> map(IdAndName idAndName, Span exceptionSpan, long rootSpanId, String uriTemplate) {
        Span.Event exceptionEvent = ExceptionAttributeUtils.findExceptionEvent(exceptionSpan);
        if (exceptionEvent == null) {
            return Optional.empty();
        }

        final Map<String, AttributeValue> eventAttrs = OtlpTraceMapperUtils.getAttributeValueMap(exceptionEvent.getAttributesList());
        final Map<String, AttributeValue> spanAttrs = OtlpTraceMapperUtils.getAttributeValueMap(exceptionSpan.getAttributesList());
        final String exceptionType = ExceptionAttributeUtils.resolveExceptionType(eventAttrs, spanAttrs);
        if (!StringUtils.hasLength(exceptionType)) {
            return Optional.empty();
        }

        final String exceptionMessage = cap(AttributeUtils.getAttributeStringValue(eventAttrs, ATTRIBUTE_KEY_EXCEPTION_MESSAGE, EMPTY), messageMaxBytes, messageTruncatedCounter);
        final String stackTraceStr = AttributeUtils.getAttributeStringValue(eventAttrs, ATTRIBUTE_KEY_EXCEPTION_STACKTRACE, EMPTY);

        final long eventTime = exceptionEvent.getTimeUnixNano() > 0
                ? TimeUnit.NANOSECONDS.toMillis(exceptionEvent.getTimeUnixNano())
                : TimeUnit.NANOSECONDS.toMillis(exceptionSpan.getStartTimeUnixNano());

        // OTel has no exception-chain id. Since ExceptionMetaDataBo.spanId is the (shared) root
        // span id, use the exception-bearing span's id as the exceptionId so multiple exceptions
        // in one transaction stay distinct under (transactionId, rootSpanId, exceptionId).
        final long exceptionSpanId = OtlpTraceMapperUtils.getSpanId(exceptionSpan.getSpanId());
        final List<StackTraceElementWrapperBo> stackTrace = parseStackTrace(stackTraceStr);

        ExceptionWrapperBo wrapper = new ExceptionWrapperBo(
                exceptionType,
                exceptionMessage,
                eventTime,
                exceptionSpanId,
                0,
                stackTrace
        );

        final OtelServerTraceId transactionId = new OtelServerTraceId(exceptionSpan.getTraceId().toByteArray());
        ExceptionMetaDataBo bo = new ExceptionMetaDataBo(
                transactionId,
                rootSpanId,
                ServiceType.OPENTELEMETRY_SERVER.getCode(),
                idAndName.applicationName(),
                idAndName.agentId(),
                uriTemplate
        );
        bo.setExceptionWrapperBos(List.of(wrapper));
        return Optional.of(bo);
    }

    List<StackTraceElementWrapperBo> parseStackTrace(String stackTrace) {
        List<StackTraceElementWrapperBo> result = new ArrayList<>();
        if (!StringUtils.hasLength(stackTrace)) {
            return result;
        }

        for (String line : stackTrace.split("\n")) {
            // Cap the number of frames: the flattened stacktrace is client-supplied, so its frame
            // count is unbounded (up to the request-size limit) without this guard.
            if (stackTraceMaxDepth > 0 && result.size() >= stackTraceMaxDepth) {
                stackTraceDepthTruncatedCounter.increment();
                break;
            }

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

            result.add(new StackTraceElementWrapperBo(
                    cap(className, frameMaxBytes, frameValueTruncatedCounter),
                    cap(fileName, frameMaxBytes, frameValueTruncatedCounter),
                    lineNumber,
                    cap(methodName, frameMaxBytes, frameValueTruncatedCounter)));
        }
        return result;
    }

    /**
     * Truncates {@code value} to at most {@code maxBytes} UTF-8 bytes (never splitting a multi-byte
     * character) and increments {@code counter} when truncation actually occurred. Returns the
     * original reference when already within the limit.
     *
     * <p>If truncation would yield an empty string for a non-empty input (i.e. {@code maxBytes} is
     * smaller than the first code point's UTF-8 length), the original is kept instead: an empty
     * value would violate {@link StackTraceElementWrapperBo}'s non-empty className/methodName
     * contract. With the {@code >= 1} byte caps validated in the constructor this only applies to a
     * leading multi-byte character.
     */
    private static String cap(String value, int maxBytes, Counter counter) {
        final String truncated = Utf8.truncate(value, maxBytes);
        if (truncated == null) {
            return value; // already within the limit
        }
        if (truncated.isEmpty() && !value.isEmpty()) {
            return value; // would drop the whole value; keep the original rather than emit ""
        }
        counter.increment();
        return truncated;
    }
}
