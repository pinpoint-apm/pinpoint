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

package com.navercorp.pinpoint.otlp.log.collector.service;

import com.navercorp.pinpoint.collector.service.ExceptionMetaDataService;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.log.collector.OtlpLogRejectReason;
import com.navercorp.pinpoint.otlp.log.collector.OtlpLogRejectedRecords;
import com.navercorp.pinpoint.otlp.log.collector.mapper.ExceptionLogCandidate;
import com.navercorp.pinpoint.otlp.log.collector.mapper.ExceptionLogDeduplicator;
import com.navercorp.pinpoint.otlp.log.collector.mapper.ExceptionLogSelector;
import com.navercorp.pinpoint.otlp.log.collector.mapper.OtlpLogExceptionMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.IdAndName;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpIdValidator;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Transport-agnostic OTLP logs ingestion: selects the exception LogRecords out of an export request
 * and stores them as Error Analysis records. Shared by the gRPC ({@link GrpcOtlpLogService}) and HTTP
 * ({@code OtlpLogController}) transports.
 *
 * <p>Per record, in order: {@code exception.*} key scan and blacklist ({@link ExceptionLogSelector}),
 * trace-context validity ({@link OtlpIdValidator}), the sampled-flag gate, in-request de-duplication
 * ({@link ExceptionLogDeduplicator}), then mapping ({@link OtlpLogExceptionMapper}) and storage. The
 * first two checks look at attribute keys only — no value parsing — so the ~99% of records that are
 * ordinary log lines cost little. There is deliberately no cross-request or cross-signal
 * de-duplication cache: the receiver's memory must not grow with log volume.
 */
@Service
public class OtlpLogExportService {

    /** W3C trace-flags sampled bit, in the low 8 bits of {@code LogRecord.flags}. */
    private static final int TRACE_FLAGS_SAMPLED = 0x01;

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ThrottledLogger throttledLogger = ThrottledLogger.getUncountedIntervalLogger(logger);

    private final ExceptionLogSelector selector;
    private final OtlpLogExceptionMapper exceptionMapper;
    // Null when the exceptiontrace storage module is not enabled in this process.
    private final ExceptionMetaDataService exceptionMetaDataService;
    private final OtlpLogIngestMetrics ingestMetrics;
    // Same resource resolution flag as the trace path, so an application resolves to the same
    // (applicationName, agentId) on both signals.
    private final boolean allowApplicationNameFallback;
    // Whether to store records whose trace flags say the trace was not sampled. Off by default: no
    // span will ever exist for them, so the transaction link is dead; on, they still feed the
    // Error Analysis aggregates.
    private final boolean storeUnsampled;

    public OtlpLogExportService(ExceptionLogSelector selector,
                                OtlpLogExceptionMapper exceptionMapper,
                                Optional<ExceptionMetaDataService> exceptionMetaDataService,
                                OtlpLogIngestMetrics ingestMetrics,
                                @Value("${pinpoint.collector.otlptrace.application-name-fallback.enabled:false}") boolean allowApplicationNameFallback,
                                @Value("${pinpoint.collector.otlplog.exception.store-unsampled:false}") boolean storeUnsampled) {
        this.selector = Objects.requireNonNull(selector, "selector");
        this.exceptionMapper = Objects.requireNonNull(exceptionMapper, "exceptionMapper");
        this.exceptionMetaDataService = exceptionMetaDataService.orElse(null);
        this.ingestMetrics = Objects.requireNonNull(ingestMetrics, "ingestMetrics");
        this.allowApplicationNameFallback = allowApplicationNameFallback;
        this.storeUnsampled = storeUnsampled;
        if (this.exceptionMetaDataService == null) {
            logger.warn("No ExceptionMetaDataService in this context (pinpoint.modules.collector.exceptiontrace.enabled=false?). "
                    + "OTLP exception LogRecords will be received and dropped (reason=storage_unavailable).");
        }
    }

    public OtlpLogExportResult export(List<ResourceLogs> resourceLogsList, OtlpTransport transport) {
        final OtlpLogRejectedRecords rejected = new OtlpLogRejectedRecords();
        final ExceptionLogDeduplicator deduplicator = new ExceptionLogDeduplicator();

        for (ResourceLogs resourceLogs : resourceLogsList) {
            final int recordCount = countRecords(resourceLogs);
            ingestMetrics.recordReceived(transport, recordCount);

            final Map<String, AttributeValue> resourceAttributes =
                    OtlpTraceMapperUtils.getAttributeValueMap(resourceLogs.getResource().getAttributesList());
            final IdAndName idAndName = resolveId(resourceAttributes);
            if (idAndName == null) {
                rejected.add(OtlpLogRejectReason.INVALID_RESOURCE, recordCount);
                continue;
            }
            final String sdkLanguage = AttributeUtils.getAttributeStringValue(
                    resourceAttributes, OtlpTraceConstants.ATTRIBUTE_KEY_TELEMETRY_SDK_LANGUAGE, null);

            for (ScopeLogs scopeLogs : resourceLogs.getScopeLogsList()) {
                final String scopeName = scopeLogs.getScope().getName();
                for (LogRecord record : scopeLogs.getLogRecordsList()) {
                    select(record, scopeName, idAndName, sdkLanguage, deduplicator, rejected);
                }
            }
        }

        int stored = 0;
        int storedUnsampled = 0;
        for (ExceptionLogCandidate candidate : deduplicator.winners()) {
            final ExceptionMetaDataBo bo;
            try {
                final Optional<ExceptionMetaDataBo> mapped = exceptionMapper.map(candidate);
                if (mapped.isEmpty()) {
                    rejected.add(OtlpLogRejectReason.NO_EXCEPTION_TYPE);
                    continue;
                }
                bo = mapped.get();
            } catch (Exception e) {
                rejected.add(OtlpLogRejectReason.MAPPING_ERROR);
                throttledLogger.warn("Failed to map exception LogRecord", e);
                continue;
            }
            if (exceptionMetaDataService == null) {
                rejected.add(OtlpLogRejectReason.STORAGE_UNAVAILABLE);
                continue;
            }
            try {
                exceptionMetaDataService.save(bo);
            } catch (Exception e) {
                // The store is a Kafka producer; a failure is counted, not retried by the exporter
                // (the record is gone either way and a batch retry would duplicate the rest).
                ingestMetrics.storeError();
                throttledLogger.warn("Failed to store exception LogRecord", e);
                continue;
            }
            stored++;
            if (candidate.unsampled()) {
                storedUnsampled++;
            }
        }

        ingestMetrics.recordStored(transport, stored);
        ingestMetrics.recordUnsampledContext(transport, storedUnsampled);
        for (Map.Entry<OtlpLogRejectReason, Long> entry : rejected.countByReason().entrySet()) {
            ingestMetrics.recordDropped(transport, entry.getKey(), entry.getValue());
        }
        return new OtlpLogExportResult(stored, rejected);
    }

    private void select(LogRecord record, String scopeName, IdAndName idAndName, String sdkLanguage,
                        ExceptionLogDeduplicator deduplicator, OtlpLogRejectedRecords rejected) {
        if (!selector.hasExceptionAttribute(record)) {
            rejected.add(OtlpLogRejectReason.NO_EXCEPTION);
            return;
        }
        if (selector.isBlacklisted(scopeName, idAndName.applicationName())) {
            rejected.add(OtlpLogRejectReason.BLACKLISTED);
            return;
        }
        if (!OtlpIdValidator.isValidTraceId(record.getTraceId()) || !OtlpIdValidator.isValidSpanId(record.getSpanId())) {
            rejected.add(OtlpLogRejectReason.NO_TRACE_CONTEXT);
            return;
        }
        final boolean unsampled = isUnsampled(record);
        if (unsampled && !storeUnsampled) {
            rejected.add(OtlpLogRejectReason.UNSAMPLED_CONTEXT);
            return;
        }
        final ExceptionLogCandidate candidate = new ExceptionLogCandidate(idAndName, sdkLanguage, record, unsampled);
        if (!deduplicator.offer(candidate)) {
            rejected.add(OtlpLogRejectReason.DUPLICATE);
        }
    }

    /**
     * The low 8 bits of {@code flags} are the W3C trace flags of the span the record was logged in;
     * bit 0 is {@code sampled}. A record with a valid trace context but a clear sampled bit belongs
     * to a trace the SDK sampler dropped, so no span will ever reach storage. (The proto cannot
     * distinguish "flags not set" from "not sampled"; SDKs that propagate a context set the flags.)
     */
    static boolean isUnsampled(LogRecord record) {
        return (record.getFlags() & TRACE_FLAGS_SAMPLED) == 0;
    }

    private IdAndName resolveId(Map<String, AttributeValue> resourceAttributes) {
        try {
            return OtlpTraceMapperUtils.getId(resourceAttributes, allowApplicationNameFallback);
        } catch (Exception e) {
            throttledLogger.warn("Failed to resolve application/agent from ResourceLogs: {}", e.getMessage());
            return null;
        }
    }

    static int countRecords(ResourceLogs resourceLogs) {
        int count = 0;
        for (ScopeLogs scopeLogs : resourceLogs.getScopeLogsList()) {
            count += scopeLogs.getLogRecordsCount();
        }
        return count;
    }
}
