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

package com.navercorp.pinpoint.otlp.trace.collector.inspector.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.IdAndName;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAgentCacheKey;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAgentInfoMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAgentStartTimeResolver;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpResourceIdResolver;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpServiceNotFoundException;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils;
import com.navercorp.pinpoint.otlp.trace.collector.service.HbaseOtlpAgentInfoService;
import com.navercorp.pinpoint.otlp.trace.collector.service.HbaseOtlpApplicationIndexV2Service;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import com.navercorp.pinpoint.otlp.trace.inspector.InspectorAgentIdentity;
import com.navercorp.pinpoint.otlp.trace.inspector.MappingResult;
import com.navercorp.pinpoint.otlp.trace.inspector.OtlpMetricInspectorMapper;
import com.navercorp.pinpoint.otlp.trace.inspector.OtlpMetricPointReader;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Transport-agnostic OTLP metrics ingestion, shared by {@link GrpcOtlpMetricService} and
 * {@code OtlpMetricController}. Per {@code ResourceMetrics} (one JVM): resolve the Pinpoint identity
 * with the trace path's resolver, map the metrics to an {@link AgentStatBo}
 * ({@link OtlpMetricInspectorMapper}), register the agent in HBase if this process has not seen it
 * (the cache is the one the trace path uses, so an agent whose spans arrived first is not
 * re-inserted), and hand the stat to every {@link AgentStatService} in the context (the Inspector's
 * Kafka/Pinot store).
 *
 * <p>Store failures are counted, not surfaced as a retryable error: the stat write is a Kafka
 * producer call and a whole-batch retry would double-count the Resources that were stored.
 */
@Service
public class OtlpMetricExportService {

    // Same sanity window as the trace path's process.creation.time resolver: a point time outside it is
    // not a time (unset, a non-epoch clock, a clock that jumped ahead) and never becomes a start time.
    private static final long MIN_VALID_EPOCH_NANOS = TimeUnit.MILLISECONDS.toNanos(946_684_800_000L); // 2000-01-01T00:00:00Z
    private static final long MAX_FUTURE_SKEW_NANOS = TimeUnit.MINUTES.toNanos(5);

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ThrottledLogger throttledLogger = ThrottledLogger.getUncountedIntervalLogger(logger);

    private final OtlpResourceIdResolver idResolver;
    private final OtlpAgentStartTimeResolver agentStartTimeResolver;
    private final OtlpAgentInfoMapper agentInfoMapper;
    private final HbaseOtlpAgentInfoService agentInfoService;
    private final HbaseOtlpApplicationIndexV2Service applicationIndexV2Service;
    private final Cache<String, Boolean> agentIdCache;
    private final OtlpMetricInspectorMapper mapper;
    private final AgentStatService[] agentStatServices;
    private final OtlpMetricIngestMetrics ingestMetrics;

    public OtlpMetricExportService(OtlpResourceIdResolver idResolver,
                                   OtlpAgentStartTimeResolver agentStartTimeResolver,
                                   OtlpAgentInfoMapper agentInfoMapper,
                                   @Qualifier("hbaseOtlpAgentInfoService") HbaseOtlpAgentInfoService agentInfoService,
                                   @Qualifier("hbaseOtlpApplicationIndexV2Service") HbaseOtlpApplicationIndexV2Service applicationIndexV2Service,
                                   @Qualifier("otlpAgentIdCache") Cache<String, Boolean> agentIdCache,
                                   OtlpMetricInspectorMapper mapper,
                                   AgentStatService[] agentStatServices,
                                   OtlpMetricIngestMetrics ingestMetrics) {
        this.idResolver = Objects.requireNonNull(idResolver, "idResolver");
        this.agentStartTimeResolver = Objects.requireNonNull(agentStartTimeResolver, "agentStartTimeResolver");
        this.agentInfoMapper = Objects.requireNonNull(agentInfoMapper, "agentInfoMapper");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.applicationIndexV2Service = Objects.requireNonNull(applicationIndexV2Service, "applicationIndexV2Service");
        this.agentIdCache = Objects.requireNonNull(agentIdCache, "agentIdCache");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.agentStatServices = Objects.requireNonNull(agentStatServices, "agentStatServices");
        this.ingestMetrics = Objects.requireNonNull(ingestMetrics, "ingestMetrics");
        if (agentStatServices.length == 0) {
            throw new IllegalStateException("No AgentStatService in this context: the Inspector storage module is required");
        }
        logger.info("OTLP metrics -> Inspector: dialects={}, agentStatServices={}", mapper.getVocabulary().dialectNames(), agentStatServices.length);
    }

    public OtlpMetricExportResult export(List<ResourceMetrics> resourceMetricsList, OtlpTransport transport) {
        final Map<OtlpMetricResourceRejectReason, Long> rejected = new EnumMap<>(OtlpMetricResourceRejectReason.class);
        int storedStats = 0;

        for (ResourceMetrics resourceMetrics : resourceMetricsList) {
            final List<Metric> metrics = collectMetrics(resourceMetrics);
            final int pointCount = countPoints(metrics);
            ingestMetrics.pointReceived(transport, pointCount);

            final Map<String, AttributeValue> resourceAttributes =
                    OtlpTraceMapperUtils.getAttributeValueMap(resourceMetrics.getResource().getAttributesList());
            final IdAndName idAndName;
            try {
                idAndName = idResolver.resolve(resourceAttributes);
            } catch (OtlpServiceNotFoundException e) {
                throttledLogger.warn("Service not found. serviceName={}, applicationName={}",
                        e.getServiceName(), OtlpTraceMapperUtils.getApplicationName(resourceAttributes));
                rejected.merge(OtlpMetricResourceRejectReason.SERVICE_NOT_FOUND, (long) pointCount, Long::sum);
                continue;
            } catch (Exception e) {
                throttledLogger.warn("Failed to resolve application/agent from ResourceMetrics: {}", e.getMessage());
                rejected.merge(OtlpMetricResourceRejectReason.INVALID_RESOURCE, (long) pointCount, Long::sum);
                continue;
            }

            final long agentStartTime = agentStartTime(resourceAttributes, metrics, transport);
            final InspectorAgentIdentity identity = new InspectorAgentIdentity(
                    idAndName.serviceName(), idAndName.applicationName(), idAndName.agentId(), agentStartTime);

            final MappingResult result;
            try {
                result = mapper.map(identity, resourceMetrics);
            } catch (Exception e) {
                throttledLogger.warn("Failed to map ResourceMetrics. applicationName={}, agentId={}",
                        identity.applicationName(), identity.agentId(), e);
                continue;
            }
            ingestMetrics.mappingResult(transport, result);
            final AgentStatBo agentStatBo = result.getAgentStatBo();
            if (agentStatBo == null) {
                continue;
            }

            registerAgent(idAndName, agentStartTime, metrics, resourceAttributes);
            if (save(agentStatBo)) {
                storedStats++;
            }
        }

        ingestMetrics.statStored(transport, storedStats);
        for (Map.Entry<OtlpMetricResourceRejectReason, Long> entry : rejected.entrySet()) {
            ingestMetrics.resourceRejected(transport, entry.getKey(), entry.getValue());
        }
        return new OtlpMetricExportResult(storedStats, rejected);
    }

    private boolean save(AgentStatBo agentStatBo) {
        boolean saved = false;
        for (AgentStatService agentStatService : agentStatServices) {
            try {
                agentStatService.save(agentStatBo);
                saved = true;
            } catch (Exception e) {
                ingestMetrics.agentStatInsertError();
                throttledLogger.warn("Failed to store agentStat. applicationName={}, agentId={}",
                        agentStatBo.getApplicationName(), agentStatBo.getAgentId(), e);
            }
        }
        return saved;
    }

    /**
     * AgentInfo + application index for a process session this collector has not registered yet.
     * The key ({@link OtlpAgentCacheKey#metric}) carries the start time: every source of it is fixed
     * for the life of the process, so an agent restarted under the same agentId registers its new
     * session. With no start time (0) the agent is registered once. The trace path registers in its
     * own key space (once per agent), so an application that sends both gets a row from each path.
     * A failed insert is not cached so the next export retries.
     *
     * <p>With no start time the row gets an approximation ({@link #registrationStartTime}) instead of
     * 0, which would be a session starting in 1970; the key keeps 0 so a later export does not
     * register the agent again.
     */
    private void registerAgent(IdAndName idAndName, long agentStartTime, List<Metric> metrics,
                               Map<String, AttributeValue> resourceAttributes) {
        final String cacheKey = OtlpAgentCacheKey.metric(idAndName.serviceUid(), idAndName.agentId(), agentStartTime);
        if (agentIdCache.getIfPresent(cacheKey) != null) {
            return;
        }
        try {
            final long startTime = registrationStartTime(agentStartTime, metrics);
            final AgentInfoBo agentInfoBo = agentInfoMapper.map(idAndName, startTime, resourceAttributes);
            agentInfoService.insert(agentInfoBo);
            applicationIndexV2Service.insert(idAndName.serviceUid(), agentInfoBo);
            agentIdCache.put(cacheKey, Boolean.TRUE);
        } catch (Exception e) {
            ingestMetrics.agentInfoInsertError();
            throttledLogger.warn("Failed to insert agentInfoBo. applicationName={}, agentId={}",
                    idAndName.applicationName(), idAndName.agentId(), e);
        }
    }

    /**
     * Start time of the AgentInfo row: the agent start time when one is known, else the earliest
     * point time of this export (the first export seen, like the trace path's span start time), else
     * the receive time. Only the row gets it; the registration key and the series keys keep 0, so a
     * later export does not register the agent again.
     */
    static long registrationStartTime(long agentStartTime, List<Metric> metrics) {
        if (agentStartTime > 0) {
            return agentStartTime;
        }
        final long earliestPointTime = earliestPointTimeMs(metrics);
        if (earliestPointTime > 0) {
            return earliestPointTime;
        }
        return System.currentTimeMillis();
    }

    /** Earliest valid {@code time_unix_nano} of the points, or -1 when there is none. */
    static long earliestPointTimeMs(List<Metric> metrics) {
        final long latestValid = latestValidNanos();
        long earliest = Long.MAX_VALUE;
        for (Metric metric : metrics) {
            switch (metric.getDataCase()) {
                case SUM:
                    for (NumberDataPoint point : metric.getSum().getDataPointsList()) {
                        earliest = min(earliest, point.getTimeUnixNano(), latestValid);
                    }
                    break;
                case GAUGE:
                    for (NumberDataPoint point : metric.getGauge().getDataPointsList()) {
                        earliest = min(earliest, point.getTimeUnixNano(), latestValid);
                    }
                    break;
                case HISTOGRAM:
                    for (HistogramDataPoint point : metric.getHistogram().getDataPointsList()) {
                        earliest = min(earliest, point.getTimeUnixNano(), latestValid);
                    }
                    break;
                default:
                    break;
            }
        }
        if (earliest == Long.MAX_VALUE) {
            return -1;
        }
        return TimeUnit.NANOSECONDS.toMillis(earliest);
    }

    /**
     * Process start time for the session fields, best source first: the {@code process.creation.time}
     * resource attribute (the trace path's rule, exact), a start-time metric such as Micrometer's
     * {@code process.start.time} (exact), the earliest {@code start_time_unix_nano} of the Resource's
     * CUMULATIVE Sum and Histogram points (the SDK start, seconds after the JVM start; DELTA points and
     * gauges are skipped because their start_time may move every export), else 0.
     */
    long agentStartTime(Map<String, AttributeValue> resourceAttributes, List<Metric> metrics, OtlpTransport transport) {
        final long resolved = agentStartTimeResolver.resolve(resourceAttributes);
        if (resolved != OtlpAgentStartTimeResolver.UNSET) {
            ingestMetrics.agentStartTime(transport, OtlpMetricIngestMetrics.START_TIME_CREATION_TIME);
            return resolved;
        }
        final long fromMetric = mapper.findProcessStartTime(metrics);
        if (fromMetric != OtlpMetricInspectorMapper.NO_START_TIME) {
            ingestMetrics.agentStartTime(transport, OtlpMetricIngestMetrics.START_TIME_METRIC);
            return fromMetric;
        }
        final long fromPoints = earliestPointStartTimeMs(metrics);
        if (fromPoints > 0) {
            ingestMetrics.agentStartTime(transport, OtlpMetricIngestMetrics.START_TIME_POINT);
            return fromPoints;
        }
        ingestMetrics.agentStartTime(transport, OtlpMetricIngestMetrics.START_TIME_NONE);
        return 0;
    }

    /**
     * Earliest valid {@code start_time_unix_nano} of the CUMULATIVE Sum and Histogram points (the SDK
     * start). This value is the session's identity (the AgentInfo row, the registration key, the
     * series-state keys), so only a start_time that is fixed for the life of the process may be used:
     * <ul>
     *   <li>DELTA points are skipped: their start_time is the start of the export interval and moves
     *   every export.</li>
     *   <li>Gauges are skipped: a gauge has no aggregation temporality to tell, and its optional
     *   start_time is the previous collection under a DELTA reader.</li>
     *   <li>A start_time outside the sanity window (before 2000, or ahead of this collector's clock) is
     *   not a time and is skipped.</li>
     * </ul>
     */
    static long earliestPointStartTimeMs(List<Metric> metrics) {
        final long latestValid = latestValidNanos();
        long earliest = Long.MAX_VALUE;
        for (Metric metric : metrics) {
            switch (metric.getDataCase()) {
                case SUM:
                    if (metric.getSum().getAggregationTemporality() == AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA) {
                        break;
                    }
                    for (NumberDataPoint point : metric.getSum().getDataPointsList()) {
                        earliest = min(earliest, point.getStartTimeUnixNano(), latestValid);
                    }
                    break;
                case HISTOGRAM:
                    if (metric.getHistogram().getAggregationTemporality() == AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA) {
                        break;
                    }
                    for (HistogramDataPoint point : metric.getHistogram().getDataPointsList()) {
                        earliest = min(earliest, point.getStartTimeUnixNano(), latestValid);
                    }
                    break;
                default:
                    break;
            }
        }
        if (earliest == Long.MAX_VALUE) {
            return -1;
        }
        return TimeUnit.NANOSECONDS.toMillis(earliest);
    }

    private static long latestValidNanos() {
        return TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()) + MAX_FUTURE_SKEW_NANOS;
    }

    /** The smaller of {@code current} and {@code timeUnixNano}, the latter only when it is a valid time. */
    private static long min(long current, long timeUnixNano, long latestValidNanos) {
        if (timeUnixNano >= MIN_VALID_EPOCH_NANOS && timeUnixNano <= latestValidNanos && timeUnixNano < current) {
            return timeUnixNano;
        }
        return current;
    }

    static List<Metric> collectMetrics(ResourceMetrics resourceMetrics) {
        final List<Metric> metrics = new ArrayList<>();
        for (ScopeMetrics scopeMetrics : resourceMetrics.getScopeMetricsList()) {
            metrics.addAll(scopeMetrics.getMetricsList());
        }
        return metrics;
    }

    static int countPoints(List<Metric> metrics) {
        int count = 0;
        for (Metric metric : metrics) {
            count += OtlpMetricPointReader.countPoints(metric);
        }
        return count;
    }
}
