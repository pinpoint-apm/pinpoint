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

package com.navercorp.pinpoint.otlp.log.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionWrapperBo;
import com.navercorp.pinpoint.common.server.bo.exception.StackTraceElementWrapperBo;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.ExceptionAttributeUtils;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.ExceptionFieldLimits;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.IdAndName;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpExceptionMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.logs.v1.LogRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Maps an exception LogRecord to an {@link ExceptionMetaDataBo}, the same record the trace path
 * builds from an {@code exception} span event, so both land in the same Error Analysis table.
 *
 * <p>Differences from the span-event path, on purpose:
 * <ul>
 *     <li><b>spanId is the record's own {@code span_id}</b> (the span the exception was logged in).
 *     The trace path stores the transaction <i>root</i> span id there so the web links the exception
 *     to the stored root span, but a LogRecord carries no parent/root information, the ingest path
 *     must not read HBase to find it, and a process-local span cache would rarely hit (logs are
 *     batched every 1s, spans every 5s, so the record usually arrives first). The detail view keys
 *     on the stored {@code (transactionId, spanId, exceptionId)}, so it still works; only the call
 *     tree link differs.</li>
 *     <li>{@code exceptionId} is also the record's {@code span_id}, as the trace path uses the
 *     exception-bearing span id — so a span-event exception and a log exception of the same span
 *     share a key.</li>
 *     <li>Timestamp falls back {@code time_unix_nano} &rarr; {@code observed_time_unix_nano} &rarr;
 *     receive time (both fields were seen empty in practice).</li>
 *     <li>Message falls back {@code exception.message} &rarr; string body &rarr; empty; stack trace
 *     may be absent (Go's otelslog emits type and message only).</li>
 *     <li>{@code uriTemplate} is the record's {@code http.route} when present, otherwise {@code ""}
 *     — never a raw path and never null, like the trace path.</li>
 * </ul>
 * Stack trace parsing (language parsers, depth cap, raw fallback) is delegated to the trace path's
 * {@link OtlpExceptionMapper} so both signals parse identically.
 */
@Component
public class OtlpLogExceptionMapper {

    private static final String EMPTY = "";
    private static final String TRUNCATED_METRIC = "collector.otlplog.exception.truncated";

    private final OtlpExceptionMapper stackTraceParser;
    private final int messageMaxBytes;
    private final int typeMaxBytes;
    private final int uriTemplateMaxBytes;
    private final Counter messageTruncatedCounter;
    private final Counter typeTruncatedCounter;
    private final Counter uriTemplateTruncatedCounter;
    private final LongSupplier clock;

    // Two constructors (the package-private one injects a clock for tests), so Spring needs to be told
    // which one to use; without this the context fails with "No default constructor found".
    @Autowired
    public OtlpLogExceptionMapper(OtlpExceptionMapper stackTraceParser,
                                  // Same cap as the trace path by default (its key is the fallback), so
                                  // the Pinot errorMessage column is bounded alike for both signals.
                                  @Value("${pinpoint.collector.otlplog.exception.message-max-bytes:${pinpoint.collector.otlptrace.exception.message-max-bytes:2048}}") int messageMaxBytes,
                                  @Value("${pinpoint.collector.otlplog.exception.type-max-bytes:${pinpoint.collector.otlptrace.exception.type-max-bytes:1024}}") int typeMaxBytes,
                                  @Value("${pinpoint.collector.otlplog.exception.uri-template-max-bytes:${pinpoint.collector.otlptrace.exception.uri-template-max-bytes:1024}}") int uriTemplateMaxBytes,
                                  MeterRegistry meterRegistry) {
        this(stackTraceParser, messageMaxBytes, typeMaxBytes, uriTemplateMaxBytes, meterRegistry, System::currentTimeMillis);
    }

    OtlpLogExceptionMapper(OtlpExceptionMapper stackTraceParser, int messageMaxBytes, MeterRegistry meterRegistry, LongSupplier clock) {
        this(stackTraceParser, messageMaxBytes, OtlpExceptionMapper.DEFAULT_TYPE_MAX_BYTES, OtlpExceptionMapper.DEFAULT_URI_TEMPLATE_MAX_BYTES, meterRegistry, clock);
    }

    OtlpLogExceptionMapper(OtlpExceptionMapper stackTraceParser, int messageMaxBytes, int typeMaxBytes, int uriTemplateMaxBytes,
                           MeterRegistry meterRegistry, LongSupplier clock) {
        this.stackTraceParser = Objects.requireNonNull(stackTraceParser, "stackTraceParser");
        if (messageMaxBytes < 1) {
            throw new IllegalArgumentException("messageMaxBytes must be >= 1: " + messageMaxBytes);
        }
        if (typeMaxBytes < 1) {
            throw new IllegalArgumentException("typeMaxBytes must be >= 1: " + typeMaxBytes);
        }
        if (uriTemplateMaxBytes < 1) {
            throw new IllegalArgumentException("uriTemplateMaxBytes must be >= 1: " + uriTemplateMaxBytes);
        }
        this.messageMaxBytes = messageMaxBytes;
        this.typeMaxBytes = typeMaxBytes;
        this.uriTemplateMaxBytes = uriTemplateMaxBytes;
        this.messageTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "message");
        this.typeTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "type");
        this.uriTemplateTruncatedCounter = meterRegistry.counter(TRUNCATED_METRIC, "field", "uri_template");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @return the mapped record, or empty when no exception type can be resolved
     * ({@code exception.type}, then {@code error.type}) — an exception record without a type has no
     * grouping key in Error Analysis
     */
    public Optional<ExceptionMetaDataBo> map(ExceptionLogCandidate candidate) {
        final LogRecord record = candidate.record();
        final IdAndName idAndName = candidate.idAndName();
        final Map<String, AttributeValue> attributes = OtlpTraceMapperUtils.getAttributeValueMap(record.getAttributesList());

        // The same resolver the trace path applies to (event attrs, span attrs); a LogRecord has only
        // one attribute set, so it serves as both — exception.type first, error.type as the fallback.
        final String resolvedType = ExceptionAttributeUtils.resolveExceptionType(attributes, attributes);
        if (!StringUtils.hasLength(resolvedType)) {
            return Optional.empty();
        }
        final String exceptionType = ExceptionFieldLimits.cap(resolvedType, typeMaxBytes, typeTruncatedCounter);

        final String exceptionMessage = ExceptionFieldLimits.cap(resolveMessage(record, attributes), messageMaxBytes, messageTruncatedCounter);
        final String stackTraceStr = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_STACKTRACE, EMPTY);
        final List<StackTraceElementWrapperBo> stackTrace = stackTraceParser.parseStackTrace(stackTraceStr, candidate.sdkLanguage());

        final long spanId = OtlpTraceMapperUtils.getSpanId(record.getSpanId());
        final ExceptionWrapperBo wrapper = new ExceptionWrapperBo(
                exceptionType,
                exceptionMessage,
                resolveTime(record),
                spanId,
                0,
                stackTrace
        );

        final OtelServerTraceId transactionId = new OtelServerTraceId(record.getTraceId().toByteArray());
        final ExceptionMetaDataBo bo = new ExceptionMetaDataBo(
                transactionId,
                spanId,
                ServiceType.OPENTELEMETRY_SERVER.getCode(),
                idAndName.serviceName(),
                idAndName.applicationName(),
                idAndName.agentId(),
                resolveUriTemplate(attributes)
        );
        bo.setExceptionWrapperBos(List.of(wrapper));
        return Optional.of(bo);
    }

    private static String resolveMessage(LogRecord record, Map<String, AttributeValue> attributes) {
        final String message = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_MESSAGE, null);
        if (message != null) {
            return message;
        }
        // Appender records (log.error(msg, e)) often carry the message only in the body.
        final AnyValue body = record.getBody();
        if (body.hasStringValue()) {
            return body.getStringValue();
        }
        return EMPTY;
    }

    private long resolveTime(LogRecord record) {
        if (record.getTimeUnixNano() > 0) {
            return TimeUnit.NANOSECONDS.toMillis(record.getTimeUnixNano());
        }
        if (record.getObservedTimeUnixNano() > 0) {
            return TimeUnit.NANOSECONDS.toMillis(record.getObservedTimeUnixNano());
        }
        return clock.getAsLong();
    }

    /**
     * {@code http.route} when present and non-blank (a route template, never a raw path); otherwise
     * {@code ""}. Never null: the trace path and the native agent store {@code ""} as well, and a null
     * would be persisted as Pinot's STRING null sentinel and shown as the literal "null" next to the
     * exception name in Error Analysis. The value is sanitized like the trace path's (query string /
     * fragment dropped, control characters removed, byte cap) since a buggy SDK may send a raw URL.
     */
    String resolveUriTemplate(Map<String, AttributeValue> attributes) {
        final String route = AttributeUtils.getAttributeStringValue(attributes, OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_ROUTE, null);
        if (route == null || route.isBlank()) {
            return EMPTY;
        }
        return ExceptionFieldLimits.sanitizeUriTemplate(route, uriTemplateMaxBytes, uriTemplateTruncatedCounter);
    }
}
