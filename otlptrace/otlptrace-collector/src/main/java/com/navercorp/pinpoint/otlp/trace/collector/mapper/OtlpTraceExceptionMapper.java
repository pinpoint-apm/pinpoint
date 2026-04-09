/*
 * Copyright 2026 NAVER Corp.
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

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionWrapperBo;
import com.navercorp.pinpoint.common.server.bo.exception.StackTraceElementWrapperBo;
import com.navercorp.pinpoint.common.server.util.Base16Utils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.trace.v1.Span;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps OTLP span exception events to {@link ExceptionMetaDataBo}.
 *
 * <p>Extracts exception information from spans that carry OTLP semantic convention
 * "exception" events (name="exception") with attributes:
 * <ul>
 *   <li>{@code exception.type} — error class name</li>
 *   <li>{@code exception.message} — error message</li>
 *   <li>{@code exception.stacktrace} — raw stacktrace string (Java format)</li>
 * </ul>
 * Falls back to the span attribute {@code error.type} when {@code exception.type} is absent.
 */
@Component
public class OtlpTraceExceptionMapper {

    static final String ATTR_EXCEPTION_TYPE = "exception.type";
    static final String ATTR_EXCEPTION_MESSAGE = "exception.message";
    static final String ATTR_EXCEPTION_STACKTRACE = "exception.stacktrace";
    static final String ATTR_ERROR_TYPE = "error.type";
    static final String EVENT_NAME_EXCEPTION = "exception";

    // Matches Java stack frame lines: "	at com.example.Foo$Bar.method(Foo.java:42)"
    private static final Pattern JAVA_FRAME_PATTERN =
            Pattern.compile("\\s+at\\s+(.+)\\.([\\w<>$]+)\\((.*)\\)");

    public Optional<ExceptionMetaDataBo> map(IdAndName idAndName, Span span) {
        List<Span.Event> exceptionEvents = collectExceptionEvents(span);
        if (exceptionEvents.isEmpty()) {
            return Optional.empty();
        }

        final String traceIdHex = Base16Utils.encodeToString(span.getTraceId().toByteArray());
        final TransactionId transactionId = new OtelTransactionId(traceIdHex);
        final long spanId = OtlpTraceMapperUtils.getSpanId(span.getSpanId());
        final short serviceType = resolveServiceType(span);

        final Map<String, Object> spanAttributes = OtlpTraceMapperUtils.getAttributeToMap(span.getAttributesList());
        final String uriTemplate = resolveUriTemplate(span, spanAttributes);
        final String fallbackClassName = AttributeUtils.getStringValue(spanAttributes, ATTR_ERROR_TYPE, null);
        final long spanStartTimeMs = TimeUnit.NANOSECONDS.toMillis(span.getStartTimeUnixNano());

        ExceptionMetaDataBo bo = new ExceptionMetaDataBo(
                transactionId, spanId, serviceType,
                idAndName.applicationName(), idAndName.agentId(),
                uriTemplate
        );

        List<ExceptionWrapperBo> wrappers = new ArrayList<>(exceptionEvents.size());
        for (int i = 0; i < exceptionEvents.size(); i++) {
            ExceptionWrapperBo wrapper = buildWrapper(exceptionEvents.get(i), i, fallbackClassName, spanStartTimeMs);
            if (wrapper != null) {
                wrappers.add(wrapper);
            }
        }

        if (wrappers.isEmpty()) {
            return Optional.empty();
        }

        bo.setExceptionWrapperBos(wrappers);
        return Optional.of(bo);
    }

    private List<Span.Event> collectExceptionEvents(Span span) {
        List<Span.Event> result = new ArrayList<>();
        for (Span.Event event : span.getEventsList()) {
            if (EVENT_NAME_EXCEPTION.equals(event.getName())) {
                result.add(event);
            }
        }
        return result;
    }

    private ExceptionWrapperBo buildWrapper(Span.Event event, int index,
                                            String fallbackClassName, long spanStartTimeMs) {
        Map<String, Object> eventAttrs = OtlpTraceMapperUtils.getAttributeToMap(event.getAttributesList());

        String className = AttributeUtils.getStringValue(eventAttrs, ATTR_EXCEPTION_TYPE, null);
        if (className == null || className.isEmpty()) {
            className = fallbackClassName;
        }
        if (className == null || className.isEmpty()) {
            return null;
        }

        String message = AttributeUtils.getStringValue(eventAttrs, ATTR_EXCEPTION_MESSAGE, "");
        if (message == null) {
            message = "";
        }

        long eventTimeMs = event.getTimeUnixNano() > 0
                ? TimeUnit.NANOSECONDS.toMillis(event.getTimeUnixNano())
                : spanStartTimeMs;
        if (eventTimeMs < 0) {
            eventTimeMs = spanStartTimeMs;
        }

        String rawStackTrace = AttributeUtils.getStringValue(eventAttrs, ATTR_EXCEPTION_STACKTRACE, null);
        List<StackTraceElementWrapperBo> frames = parseStackTrace(rawStackTrace);

        return new ExceptionWrapperBo(
                className,
                message,
                eventTimeMs,
                index,  // exceptionId: sequential index within the span
                0,      // exceptionDepth: 0 = top of exception chain
                frames
        );
    }

    static List<StackTraceElementWrapperBo> parseStackTrace(String raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        List<StackTraceElementWrapperBo> frames = new ArrayList<>();
        for (String line : raw.split("\n")) {
            Matcher m = JAVA_FRAME_PATTERN.matcher(line);
            if (!m.matches()) {
                continue;
            }

            String className = m.group(1);
            String methodName = m.group(2);
            String location = m.group(3).trim();

            String fileName;
            int lineNumber;

            if ("Native Method".equals(location)) {
                fileName = "Native Method";
                lineNumber = -2;
            } else if ("Unknown Source".equals(location) || location.isEmpty()) {
                fileName = "Unknown Source";
                lineNumber = -1;
            } else {
                int colonIdx = location.lastIndexOf(':');
                if (colonIdx >= 0) {
                    fileName = location.substring(0, colonIdx);
                    try {
                        lineNumber = Integer.parseInt(location.substring(colonIdx + 1));
                    } catch (NumberFormatException e) {
                        lineNumber = -1;
                    }
                } else {
                    fileName = location;
                    lineNumber = -1;
                }
            }

            try {
                frames.add(new StackTraceElementWrapperBo(className, fileName, lineNumber, methodName));
            } catch (IllegalArgumentException ignore) {
                // skip malformed frame
            }
        }
        return frames;
    }

    private static short resolveServiceType(Span span) {
        int code = switch (span.getKind()) {
            case SPAN_KIND_CLIENT, SPAN_KIND_PRODUCER -> ServiceType.OPENTELEMETRY_CLIENT.getCode();
            case SPAN_KIND_INTERNAL -> ServiceType.OPENTELEMETRY_INTERNAL.getCode();
            default -> ServiceType.OPENTELEMETRY_SERVER.getCode();
        };
        return (short) code;
    }

    private static String resolveUriTemplate(Span span, Map<String, Object> attributes) {
        String urlPath = AttributeUtils.getStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH, null);
        if (urlPath != null) {
            return urlPath;
        }
        String httpUrl = AttributeUtils.getStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_URL, null);
        if (httpUrl != null) {
            return OtlpTraceSpanMapper.extractPath(httpUrl);
        }
        String httpTarget = AttributeUtils.getStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_TARGET, null);
        if (httpTarget != null) {
            return httpTarget;
        }
        return span.getName();
    }

    /**
     * TransactionId implementation that returns the raw OTel traceId hex string from getId().
     *
     * TransactionId.of(hex) produces TransactionIdV1(hex, 0, 0) whose getId() returns "hex^0^0",
     * which would be misinterpreted as a PinpointServerTraceId when the web layer looks up the trace.
     * This record ensures getId()/toString() returns the bare hex so it round-trips correctly via
     * ServerTraceId.of(hexString) → OtelServerTraceId.
     */
    private record OtelTransactionId(String traceIdHex) implements TransactionId {
        @Override public String getAgentId() { return traceIdHex; }
        @Override public long getAgentStartTime() { return 0; }
        @Override public long getTransactionSequence() { return 0; }
        @Override public String getId() { return traceIdHex; }
        @Override public String toString() { return traceIdHex; }
    }
}
