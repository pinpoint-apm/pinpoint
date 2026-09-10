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
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.stacktrace.StackFrame;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.stacktrace.StackFrameSink;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.stacktrace.StackTraceParser;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.stacktrace.StackTraceParserRegistry;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.proto.trace.v1.Span;
import org.springframework.beans.factory.annotation.Autowired;
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
    // Which language parser handled each stacktrace — watch the raw-fallback share to decide
    // which language parser to add next.
    private static final String PARSED_METRIC = "collector.otlptrace.exception.parsed";

    public static final int DEFAULT_MESSAGE_MAX_BYTES = 2048;
    public static final int DEFAULT_TYPE_MAX_BYTES = 1024;
    public static final int DEFAULT_URI_TEMPLATE_MAX_BYTES = 1024;
    public static final int DEFAULT_STACKTRACE_MAX_DEPTH = 256;
    public static final int DEFAULT_FRAME_MAX_BYTES = 2048;
    public static final int DEFAULT_STACKTRACE_MAX_CHARS = 262144;
    public static final int DEFAULT_STACKTRACE_LINE_MAX_CHARS = 4096;

    private static final StackTraceParserRegistry PARSER_REGISTRY = new StackTraceParserRegistry();

    // Byte-based caps for the client-supplied exception fields (unlike the native path, these arrive
    // as free-form strings from a third-party exporter). Mirrors the module's attribute/SQL/event/link
    // truncation convention (Utf8.truncate) rather than the native char-based abbreviate, since the
    // goal is bounding HBase/Pinot storage size. Truncation is surfaced via a Micrometer counter
    // (tagged by field) — the OPENTELEMETRY_TRUNCATED span annotation does not apply here because the
    // exception is a separate entity written to its own store, not part of the span.
    private final int messageMaxBytes;
    private final int typeMaxBytes;
    private final int uriTemplateMaxBytes;
    // Max number of parsed stacktrace frames. OTel delivers one flattened stacktrace string, so the
    // unbounded axis is the frame count (not the native exception-chain depth). <= 0 means unlimited.
    private final int stackTraceMaxDepth;
    private final int frameMaxBytes;
    // Input bounds applied before the regex-based parsers see the text (see parseStackTrace).
    private final int stackTraceMaxChars;
    private final int stackTraceLineMaxChars;

    private final Counter messageTruncatedCounter;
    private final Counter typeTruncatedCounter;
    private final Counter uriTemplateTruncatedCounter;
    private final Counter stackTraceDepthTruncatedCounter;
    private final Counter stackTraceBytesTruncatedCounter;
    private final Counter stackTraceLineTruncatedCounter;
    private final Counter frameValueTruncatedCounter;
    private final MeterRegistry meterRegistry;

    /** Production defaults for every bound except the three the existing tests tune. */
    public OtlpExceptionMapper(int messageMaxBytes, int stackTraceMaxDepth, int frameMaxBytes, MeterRegistry meterRegistry) {
        this(messageMaxBytes, DEFAULT_TYPE_MAX_BYTES, DEFAULT_URI_TEMPLATE_MAX_BYTES, stackTraceMaxDepth, frameMaxBytes,
                DEFAULT_STACKTRACE_MAX_CHARS, DEFAULT_STACKTRACE_LINE_MAX_CHARS, meterRegistry);
    }

    @Autowired
    public OtlpExceptionMapper(
            // Default aligned with the native agent's errormessage cap (profiler.exceptiontrace
            // .errormessage.max=2048) so both sources bound the Pinot errorMessage column alike.
            // Unit stays bytes (the agent caps chars): stricter for multi-byte text, identical for
            // the ASCII-dominant common case.
            @Value("${pinpoint.collector.otlptrace.exception.message-max-bytes:2048}") int messageMaxBytes,
            @Value("${pinpoint.collector.otlptrace.exception.type-max-bytes:1024}") int typeMaxBytes,
            @Value("${pinpoint.collector.otlptrace.exception.uri-template-max-bytes:1024}") int uriTemplateMaxBytes,
            @Value("${pinpoint.collector.otlptrace.exception.stacktrace.max-depth:256}") int stackTraceMaxDepth,
            @Value("${pinpoint.collector.otlptrace.exception.stacktrace.frame-max-bytes:2048}") int frameMaxBytes,
            @Value("${pinpoint.collector.otlptrace.exception.stacktrace.max-chars:262144}") int stackTraceMaxChars,
            @Value("${pinpoint.collector.otlptrace.exception.stacktrace.line-max-chars:4096}") int stackTraceLineMaxChars,
            MeterRegistry meterRegistry) {
        // Byte caps must be >= 1: a cap of 0 (or negative) would truncate every value to "" and, for
        // the non-empty-constrained className/methodName, make StackTraceElementWrapperBo throw.
        // (stackTraceMaxDepth / max-chars / line-max-chars are intentionally unvalidated — <= 0 is the
        // documented "unlimited".)
        if (messageMaxBytes < 1) {
            throw new IllegalArgumentException("messageMaxBytes must be >= 1: " + messageMaxBytes);
        }
        if (typeMaxBytes < 1) {
            throw new IllegalArgumentException("typeMaxBytes must be >= 1: " + typeMaxBytes);
        }
        if (uriTemplateMaxBytes < 1) {
            throw new IllegalArgumentException("uriTemplateMaxBytes must be >= 1: " + uriTemplateMaxBytes);
        }
        if (frameMaxBytes < 1) {
            throw new IllegalArgumentException("frameMaxBytes must be >= 1: " + frameMaxBytes);
        }
        this.messageMaxBytes = messageMaxBytes;
        this.typeMaxBytes = typeMaxBytes;
        this.uriTemplateMaxBytes = uriTemplateMaxBytes;
        this.stackTraceMaxDepth = stackTraceMaxDepth;
        this.frameMaxBytes = frameMaxBytes;
        this.stackTraceMaxChars = stackTraceMaxChars;
        this.stackTraceLineMaxChars = stackTraceLineMaxChars;
        this.messageTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "message");
        this.typeTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "type");
        this.uriTemplateTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "uri_template");
        this.stackTraceDepthTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "stacktrace_depth");
        this.stackTraceBytesTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "stacktrace_bytes");
        this.stackTraceLineTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "stacktrace_line");
        this.frameValueTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "frame_value");
        this.meterRegistry = meterRegistry;
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
        return map(idAndName, exceptionSpan, rootSpanId, uriTemplate, null);
    }

    /**
     * @param sdkLanguage the {@code telemetry.sdk.language} resource attribute (java, nodejs,
     *                    python, dotnet, go, ...) used to pick the stacktrace parser; {@code null}
     *                    falls back to content sniffing
     */
    public Optional<ExceptionMetaDataBo> map(IdAndName idAndName, Span exceptionSpan, long rootSpanId, String uriTemplate, String sdkLanguage) {
        Span.Event exceptionEvent = ExceptionAttributeUtils.findExceptionEvent(exceptionSpan);
        if (exceptionEvent == null) {
            return Optional.empty();
        }

        final Map<String, AttributeValue> eventAttrs = OtlpTraceMapperUtils.getAttributeValueMap(exceptionEvent.getAttributesList());
        final Map<String, AttributeValue> spanAttrs = OtlpTraceMapperUtils.getAttributeValueMap(exceptionSpan.getAttributesList());
        final String resolvedType = ExceptionAttributeUtils.resolveExceptionType(eventAttrs, spanAttrs);
        if (!StringUtils.hasLength(resolvedType)) {
            return Optional.empty();
        }
        final String exceptionType = capType(resolvedType);

        final String exceptionMessage = capMessage(AttributeUtils.getAttributeStringValue(eventAttrs, ATTRIBUTE_KEY_EXCEPTION_MESSAGE, EMPTY));
        final String stackTraceStr = AttributeUtils.getAttributeStringValue(eventAttrs, ATTRIBUTE_KEY_EXCEPTION_STACKTRACE, EMPTY);

        final long eventTime = exceptionEvent.getTimeUnixNano() > 0
                ? TimeUnit.NANOSECONDS.toMillis(exceptionEvent.getTimeUnixNano())
                : TimeUnit.NANOSECONDS.toMillis(exceptionSpan.getStartTimeUnixNano());

        // OTel has no exception-chain id. Since ExceptionMetaDataBo.spanId is the (shared) root
        // span id, use the exception-bearing span's id as the exceptionId so multiple exceptions
        // in one transaction stay distinct under (transactionId, rootSpanId, exceptionId).
        final long exceptionSpanId = OtlpTraceMapperUtils.getSpanId(exceptionSpan.getSpanId());
        final List<StackTraceElementWrapperBo> stackTrace = parseStackTrace(stackTraceStr, sdkLanguage);

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
                idAndName.serviceName(),
                idAndName.applicationName(),
                idAndName.agentId(),
                sanitizeUriTemplate(uriTemplate)
        );
        bo.setExceptionWrapperBos(List.of(wrapper));
        return Optional.of(bo);
    }

    /** Caps {@code exception.type} (the Error Analysis grouping key; deterministic, so equal types stay equal). */
    public String capType(String exceptionType) {
        return ExceptionFieldLimits.cap(exceptionType, typeMaxBytes, typeTruncatedCounter);
    }

    /** Caps {@code exception.message}. */
    public String capMessage(String message) {
        return ExceptionFieldLimits.cap(message, messageMaxBytes, messageTruncatedCounter);
    }

    /**
     * Drops any query string / fragment, strips control characters and caps the route template; never
     * null ("" = no route, the store's convention — a null would surface as Pinot's literal "null").
     */
    public String sanitizeUriTemplate(String uriTemplate) {
        return ExceptionFieldLimits.sanitizeUriTemplate(uriTemplate, uriTemplateMaxBytes, uriTemplateTruncatedCounter);
    }

    /**
     * Parses the flattened stacktrace with the language parser selected by
     * {@code telemetry.sdk.language} (or content sniffing when absent). A parser that yields zero
     * frames — a format it does not actually understand, e.g. a Ruby stack under an unmapped
     * language value — falls back to raw-line frames so the detail view keeps the original text
     * and the stack-trace grouping hash stays distinctive instead of collapsing every unparsed
     * exception into one shared "empty stack" group.
     *
     * <p>The text is bounded first ({@link ExceptionFieldLimits#boundStackTrace}: whole-text and
     * per-line caps). The language parsers are regex based and quadratic in the length of a single
     * line in their worst case, so an exporter sending one multi-megabyte line would otherwise pin a
     * worker thread for hours; with the line cap the cost is linear in the text size.
     */
    public List<StackTraceElementWrapperBo> parseStackTrace(String stackTrace, String sdkLanguage) {
        if (!StringUtils.hasLength(stackTrace)) {
            return new ArrayList<>();
        }
        final String bounded = ExceptionFieldLimits.boundStackTrace(stackTrace, stackTraceMaxChars, stackTraceLineMaxChars,
                stackTraceBytesTruncatedCounter, stackTraceLineTruncatedCounter);
        if (!StringUtils.hasLength(bounded)) {
            return new ArrayList<>();
        }

        StackTraceParser parser = PARSER_REGISTRY.select(sdkLanguage, bounded);
        StackFrameSink sink = new StackFrameSink(stackTraceMaxDepth);
        parser.parse(bounded, sink);

        if (sink.frames().isEmpty() && parser != PARSER_REGISTRY.rawFallback()) {
            parser = PARSER_REGISTRY.rawFallback();
            sink = new StackFrameSink(stackTraceMaxDepth);
            parser.parse(bounded, sink);
        }

        if (sink.isTruncated()) {
            stackTraceDepthTruncatedCounter.increment();
        }
        meterRegistry.counter(PARSED_METRIC, "parser", parser.name()).increment();

        final List<StackTraceElementWrapperBo> result = new ArrayList<>(sink.frames().size());
        for (StackFrame frame : sink.frames()) {
            result.add(new StackTraceElementWrapperBo(
                    ExceptionFieldLimits.cap(frame.className(), frameMaxBytes, frameValueTruncatedCounter),
                    ExceptionFieldLimits.cap(frame.fileName(), frameMaxBytes, frameValueTruncatedCounter),
                    frame.lineNumber(),
                    ExceptionFieldLimits.cap(frame.methodName(), frameMaxBytes, frameValueTruncatedCounter)));
        }
        return result;
    }
}
