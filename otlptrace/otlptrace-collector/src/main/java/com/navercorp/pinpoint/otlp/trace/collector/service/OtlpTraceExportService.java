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

package com.navercorp.pinpoint.otlp.trace.collector.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.navercorp.pinpoint.collector.service.AgentUriStatService;
import com.navercorp.pinpoint.collector.service.ExceptionMetaDataService;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorRejectedSpan;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperData;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Transport-agnostic OTLP trace ingestion: maps the incoming {@link ResourceSpans} into
 * SpanBo/SpanChunkBo/AgentInfoBo/ExceptionMetaDataBo/AgentUriStatBo and writes them to storage.
 * <p>
 * Shared by both the gRPC ({@code GrpcOtlpTraceService}) and HTTP ({@code OtlpTraceController})
 * transports so the mapping/insert logic — and the agentId dedup cache — live in a single place
 * (previously duplicated, with the two copies already diverging). The single {@code agentIdCache}
 * bean makes dedup effective across both transports and is thread-safe (Caffeine), unlike the
 * former per-instance {@code LRUCache} which was not safe under the worker/servlet threads.
 */
@Service
public class OtlpTraceExportService {

    public static final Supplier<ServiceUid> DEFAULT_SERVICE_UID = () -> ServiceUid.DEFAULT;

    private static final String INSERT_ERROR_METRIC = "collector.otlptrace.insert.error";

    private final Logger logger = LogManager.getLogger(this.getClass());
    // Throttled so an HBase outage (every span/chunk failing synchronously) does not flood the log.
    // Mirrors HbaseOtlpTraceService's async-path pattern (throttled WARN + a per-op error counter so
    // the true failure rate stays observable even while logs are throttled).
    private final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 100);

    @NotNull
    private final TraceService[] traceServiceList;
    private final HbaseOtlpAgentInfoService agentInfoService;
    private final HbaseOtlpApplicationIndexV2Service applicationIndexV2Service;
    @NotNull
    private final OtlpTraceMapper otlpTraceMapper;
    private final ExceptionMetaDataService exceptionMetaDataService;
    private final AgentUriStatService agentUriStatService;
    // Thread-safe, bounded dedup of already-persisted agentIds. Shared across transports so an
    // agentId first seen on gRPC is not re-inserted when it later arrives over HTTP (and vice versa).
    private final Cache<String, Boolean> agentIdCache;

    private final Counter spanInsertErrorCounter;
    private final Counter spanChunkInsertErrorCounter;
    private final Counter agentInfoInsertErrorCounter;
    private final Counter exceptionInsertErrorCounter;
    private final Counter uriStatInsertErrorCounter;

    public OtlpTraceExportService(TraceService[] traceServiceList,
                                  @Qualifier("hbaseOtlpAgentInfoService") HbaseOtlpAgentInfoService agentInfoService,
                                  @Qualifier("hbaseOtlpApplicationIndexV2Service") HbaseOtlpApplicationIndexV2Service applicationIndexV2Service,
                                  OtlpTraceMapper otlpTraceMapper,
                                  Optional<ExceptionMetaDataService> exceptionMetaDataService,
                                  Optional<AgentUriStatService> agentUriStatService,
                                  @Qualifier("otlpAgentIdCache") Cache<String, Boolean> agentIdCache,
                                  MeterRegistry meterRegistry) {
        this.traceServiceList = Objects.requireNonNull(traceServiceList, "traceServiceList");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.applicationIndexV2Service = Objects.requireNonNull(applicationIndexV2Service, "applicationIndexV2Service");
        this.otlpTraceMapper = Objects.requireNonNull(otlpTraceMapper, "otlpTraceMapper");
        this.exceptionMetaDataService = exceptionMetaDataService.orElse(null);
        this.agentUriStatService = agentUriStatService.orElse(null);
        this.agentIdCache = Objects.requireNonNull(agentIdCache, "agentIdCache");
        Objects.requireNonNull(meterRegistry, "meterRegistry");
        this.spanInsertErrorCounter = insertErrorCounter(meterRegistry, "span");
        this.spanChunkInsertErrorCounter = insertErrorCounter(meterRegistry, "spanChunk");
        this.agentInfoInsertErrorCounter = insertErrorCounter(meterRegistry, "agentInfo");
        this.exceptionInsertErrorCounter = insertErrorCounter(meterRegistry, "exception");
        this.uriStatInsertErrorCounter = insertErrorCounter(meterRegistry, "uriStat");
    }

    private static Counter insertErrorCounter(MeterRegistry meterRegistry, String op) {
        return Counter.builder(INSERT_ERROR_METRIC)
                .description("OTLP trace synchronous insert failures (HBase)")
                .tag("op", op)
                .register(meterRegistry);
    }

    public OtlpTraceExportResult export(List<ResourceSpans> resourceSpanList) {
        final OtlpTraceMapperData otlpTraceMapperData = otlpTraceMapper.map(resourceSpanList);
        // Mapping rejects (invalid ids, unlinkable/orphan spans) are client-side data faults.
        final OtlpTraceCollectorRejectedSpan clientRejected = otlpTraceMapperData.getRejectedSpan();

        int insertErrorCount = 0;
        for (SpanBo spanBo : otlpTraceMapperData.getSpanBoList()) {
            for (TraceService traceService : traceServiceList) {
                try {
                    traceService.insertSpan(spanBo);
                } catch (Exception e) {
                    insertErrorCount++;
                    spanInsertErrorCounter.increment();
                    throttledLogger.warn("Failed to insert spanBo", e);
                }
            }
        }

        for (SpanChunkBo spanChunkBo : otlpTraceMapperData.getSpanChunkBoList()) {
            for (TraceService traceService : traceServiceList) {
                try {
                    traceService.insertSpanChunk(spanChunkBo);
                } catch (Exception e) {
                    insertErrorCount++;
                    spanChunkInsertErrorCounter.increment();
                    throttledLogger.warn("Failed to insert spanChunkBo", e);
                }
            }
        }

        int agentInfoErrorCount = 0;
        for (AgentInfoBo agentInfoBo : otlpTraceMapperData.getAgentInfoBoList()) {
            if (agentIdCache.getIfPresent(agentInfoBo.getAgentId()) == null) {
                try {
                    agentInfoService.insert(agentInfoBo);
                    applicationIndexV2Service.insert(DEFAULT_SERVICE_UID, agentInfoBo);
                    agentIdCache.put(agentInfoBo.getAgentId(), Boolean.TRUE);
                } catch (Exception e) {
                    agentInfoErrorCount++;
                    agentInfoInsertErrorCounter.increment();
                    throttledLogger.warn("Failed to insert agentInfoBo", e);
                }
            }
        }

        if (exceptionMetaDataService != null) {
            for (ExceptionMetaDataBo exceptionMetaDataBo : otlpTraceMapperData.getExceptionMetaDataBoList()) {
                try {
                    exceptionMetaDataService.save(exceptionMetaDataBo);
                } catch (Exception e) {
                    exceptionInsertErrorCounter.increment();
                    throttledLogger.warn("Failed to insert exceptionMetaData", e);
                }
            }
        }

        if (agentUriStatService != null) {
            for (AgentUriStatBo agentUriStatBo : otlpTraceMapperData.getAgentUriStatBoList()) {
                try {
                    agentUriStatService.save(agentUriStatBo);
                } catch (Exception e) {
                    uriStatInsertErrorCounter.increment();
                    throttledLogger.warn("Failed to insert uriStat", e);
                }
            }
        }

        final int serverErrorCount = insertErrorCount + agentInfoErrorCount;
        return new OtlpTraceExportResult(clientRejected, serverErrorCount, buildServerMessage(insertErrorCount, agentInfoErrorCount));
    }

    private static String buildServerMessage(int insertErrorCount, int agentInfoErrorCount) {
        final StringBuilder sb = new StringBuilder();
        if (insertErrorCount > 0) {
            sb.append("insert error (").append(insertErrorCount).append(')');
        }
        if (agentInfoErrorCount > 0) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append("agentInfo error (").append(agentInfoErrorCount).append(')');
        }
        return sb.toString();
    }
}
