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

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpIngestAdmissionMetrics;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpRequestRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import com.navercorp.pinpoint.otlp.trace.inspector.InspectorDropReason;
import com.navercorp.pinpoint.otlp.trace.inspector.MappingResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * Ingest volume of the OTLP metrics receiver, per transport, in the
 * {@code collector.otlptrace.inspector.*} name space:
 * <ul>
 *     <li>{@code point.received} — data points carried by admitted export requests.</li>
 *     <li>{@code point.mapped{type}} — Inspector stat points produced, by chart family
 *     ({@code jvmGc}, {@code cpuLoad}, ...). One stat point folds several input points.</li>
 *     <li>{@code point.dropped{reason}} — input points that produced nothing: mostly
 *     {@code unknown_metric} (application and library metrics, by design) and
 *     {@code no_previous_point} (the first cumulative histogram export of a series).</li>
 *     <li>{@code resource.rejected{reason}} — points of whole Resources refused before mapping
 *     (identifier problems); these are reported back to the exporter.</li>
 *     <li>{@code stat.stored} — AgentStatBo handed to the Inspector store (one per JVM per export).</li>
 *     <li>{@code insert.error{op}} — synchronous failures writing the stat or the AgentInfo.</li>
 *     <li>{@code request.rejected{reason} / request.bytes / admission.*} — the transport-level
 *     admission slice ({@link OtlpIngestAdmissionMetrics}), same meaning as the trace path's.</li>
 * </ul>
 * Every (transport, tag) series is registered up front so it exists at zero before the first event.
 */
@Component
public class OtlpMetricIngestMetrics implements OtlpIngestAdmissionMetrics {

    public static final String POINT_RECEIVED = "collector.otlptrace.inspector.point.received";
    public static final String POINT_ACCEPTED = "collector.otlptrace.inspector.point.accepted";
    public static final String POINT_MAPPED = "collector.otlptrace.inspector.point.mapped";
    public static final String AGENT_START_TIME = "collector.otlptrace.inspector.agent-start-time";
    public static final String POINT_DROPPED = "collector.otlptrace.inspector.point.dropped";
    public static final String RESOURCE_REJECTED = "collector.otlptrace.inspector.resource.rejected";
    public static final String STAT_STORED = "collector.otlptrace.inspector.stat.stored";
    public static final String INSERT_ERROR = "collector.otlptrace.inspector.insert.error";
    public static final String REQUEST_REJECTED = "collector.otlptrace.inspector.request.rejected";
    public static final String REQUEST_BYTES = "collector.otlptrace.inspector.request.bytes";
    public static final String ADMISSION_INFLIGHT_BYTES = "collector.otlptrace.inspector.admission.inflight.bytes";
    public static final String ADMISSION_LIMIT_BYTES = "collector.otlptrace.inspector.admission.limit.bytes";
    public static final String ADMISSION_INFLIGHT_REQUESTS = "collector.otlptrace.inspector.admission.inflight.requests";
    public static final String ADMISSION_LIMIT_REQUESTS = "collector.otlptrace.inspector.admission.limit.requests";

    public static final String TAG_TRANSPORT = "transport";
    public static final String TAG_REASON = "reason";
    public static final String TAG_TYPE = "type";
    public static final String TAG_OP = "op";
    public static final String TAG_DIALECT = "dialect";
    public static final String TAG_SOURCE = "source";

    /** {@link #AGENT_START_TIME} sources, best first. */
    public static final String START_TIME_CREATION_TIME = "creation-time";
    public static final String START_TIME_METRIC = "start-time-metric";
    public static final String START_TIME_POINT = "point-start-time";
    public static final String START_TIME_NONE = "none";
    private static final List<String> START_TIME_SOURCES =
            List.of(START_TIME_CREATION_TIME, START_TIME_METRIC, START_TIME_POINT, START_TIME_NONE);

    public static final String OP_AGENT_STAT = "agentStat";
    public static final String OP_AGENT_INFO = "agentInfo";

    private static final Set<OtlpRequestRejectReason> GRPC_REQUEST_REASONS =
            EnumSet.of(OtlpRequestRejectReason.INFLIGHT_BYTES, OtlpRequestRejectReason.EXECUTOR_REJECTED);
    private static final Set<OtlpRequestRejectReason> HTTP_REQUEST_REASONS =
            EnumSet.of(OtlpRequestRejectReason.INFLIGHT_BYTES, OtlpRequestRejectReason.CONCURRENCY,
                    OtlpRequestRejectReason.PAYLOAD_TOO_LARGE, OtlpRequestRejectReason.UNSUPPORTED_ENCODING,
                    OtlpRequestRejectReason.PARSE_ERROR);

    private final Map<OtlpTransport, Counter> received = new EnumMap<>(OtlpTransport.class);
    // dialect names are a small, configuration-bounded set; registered on first use
    private final Map<OtlpTransport, Map<String, Counter>> accepted = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Map<String, Counter>> agentStartTime = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Map<AgentStatType, Counter>> mapped = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Map<InspectorDropReason, Counter>> dropped = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Map<OtlpMetricResourceRejectReason, Counter>> resourceRejected = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Counter> statStored = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Map<OtlpRequestRejectReason, Counter>> requestRejected = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, DistributionSummary> requestBytes = new EnumMap<>(OtlpTransport.class);
    private final Counter agentStatInsertError;
    private final Counter agentInfoInsertError;
    private final MeterRegistry meterRegistry;

    public OtlpMetricIngestMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry");
        for (OtlpTransport transport : OtlpTransport.values()) {
            final String transportTag = transport.tagValue();
            received.put(transport, Counter.builder(POINT_RECEIVED)
                    .description("OTLP metric data points carried by admitted export requests")
                    .tag(TAG_TRANSPORT, transportTag)
                    .register(meterRegistry));
            statStored.put(transport, Counter.builder(STAT_STORED)
                    .description("Inspector AgentStatBo handed to storage (one per JVM per export)")
                    .tag(TAG_TRANSPORT, transportTag)
                    .register(meterRegistry));
            accepted.put(transport, new ConcurrentHashMap<>());
            final Map<String, Counter> startTimeBySource = new ConcurrentHashMap<>();
            for (String source : START_TIME_SOURCES) {
                startTimeBySource.put(source, Counter.builder(AGENT_START_TIME)
                        .description("Where the agent start time of an OTLP metrics Resource came from, per export")
                        .tag(TAG_TRANSPORT, transportTag)
                        .tag(TAG_SOURCE, source)
                        .register(meterRegistry));
            }
            agentStartTime.put(transport, startTimeBySource);

            final Map<AgentStatType, Counter> mappedByType = new EnumMap<>(AgentStatType.class);
            for (AgentStatType type : AgentStatType.values()) {
                if (type == AgentStatType.UNKNOWN) {
                    continue;
                }
                mappedByType.put(type, Counter.builder(POINT_MAPPED)
                        .description("Inspector stat points produced from OTLP metrics, by chart family")
                        .tag(TAG_TRANSPORT, transportTag)
                        .tag(TAG_TYPE, type.getChartType())
                        .register(meterRegistry));
            }
            mapped.put(transport, mappedByType);

            final Map<InspectorDropReason, Counter> droppedByReason = new EnumMap<>(InspectorDropReason.class);
            for (InspectorDropReason reason : InspectorDropReason.values()) {
                droppedByReason.put(reason, Counter.builder(POINT_DROPPED)
                        .description("OTLP metric data points that produced no Inspector value, by reason")
                        .tag(TAG_TRANSPORT, transportTag)
                        .tag(TAG_REASON, reason.tag())
                        .register(meterRegistry));
            }
            dropped.put(transport, droppedByReason);

            final Map<OtlpMetricResourceRejectReason, Counter> rejectedByReason = new EnumMap<>(OtlpMetricResourceRejectReason.class);
            for (OtlpMetricResourceRejectReason reason : OtlpMetricResourceRejectReason.values()) {
                rejectedByReason.put(reason, Counter.builder(RESOURCE_REJECTED)
                        .description("Data points of ResourceMetrics rejected before mapping (identifier faults), by reason")
                        .tag(TAG_TRANSPORT, transportTag)
                        .tag(TAG_REASON, reason.tagValue())
                        .register(meterRegistry));
            }
            resourceRejected.put(transport, rejectedByReason);

            final Map<OtlpRequestRejectReason, Counter> requestByReason = new EnumMap<>(OtlpRequestRejectReason.class);
            final Set<OtlpRequestRejectReason> reasons = transport == OtlpTransport.GRPC ? GRPC_REQUEST_REASONS : HTTP_REQUEST_REASONS;
            for (OtlpRequestRejectReason reason : reasons) {
                requestByReason.put(reason, Counter.builder(REQUEST_REJECTED)
                        .description("OTLP metrics export requests refused before mapping (admission or parse failure)")
                        .tag(TAG_TRANSPORT, transportTag)
                        .tag(TAG_REASON, reason.tagValue())
                        .register(meterRegistry));
            }
            requestRejected.put(transport, requestByReason);

            requestBytes.put(transport, DistributionSummary.builder(REQUEST_BYTES)
                    .description("Wire bytes of admitted OTLP metrics export requests (gRPC serialized size / HTTP body)")
                    .baseUnit("bytes")
                    .tag(TAG_TRANSPORT, transportTag)
                    .register(meterRegistry));
        }
        this.agentStatInsertError = insertErrorCounter(meterRegistry, OP_AGENT_STAT);
        this.agentInfoInsertError = insertErrorCounter(meterRegistry, OP_AGENT_INFO);
    }

    private static Counter insertErrorCounter(MeterRegistry meterRegistry, String op) {
        return Counter.builder(INSERT_ERROR)
                .description("OTLP metrics synchronous store failures (Inspector stat / AgentInfo)")
                .tag(TAG_OP, op)
                .register(meterRegistry);
    }

    public void pointReceived(OtlpTransport transport, int count) {
        if (count > 0) {
            received.get(transport).increment(count);
        }
    }

    /** Records the accepted / mapped / dropped counts of one Resource's mapping. */
    public void mappingResult(OtlpTransport transport, MappingResult result) {
        final Map<String, Counter> acceptedByDialect = accepted.get(transport);
        for (Map.Entry<String, Integer> entry : result.getAcceptedByDialect().entrySet()) {
            if (entry.getValue() > 0) {
                acceptedByDialect.computeIfAbsent(entry.getKey(), dialect -> Counter.builder(POINT_ACCEPTED)
                                .description("OTLP metric data points folded into an Inspector value, by naming dialect")
                                .tag(TAG_TRANSPORT, transport.tagValue())
                                .tag(TAG_DIALECT, dialect)
                                .register(meterRegistry))
                        .increment(entry.getValue());
            }
        }
        final Map<AgentStatType, Counter> mappedByType = mapped.get(transport);
        for (Map.Entry<AgentStatType, Integer> entry : result.getMapped().entrySet()) {
            final Counter counter = mappedByType.get(entry.getKey());
            if (counter != null && entry.getValue() > 0) {
                counter.increment(entry.getValue());
            }
        }
        final Map<InspectorDropReason, Counter> droppedByReason = dropped.get(transport);
        for (Map.Entry<InspectorDropReason, Integer> entry : result.getDropped().entrySet()) {
            if (entry.getValue() > 0) {
                droppedByReason.get(entry.getKey()).increment(entry.getValue());
            }
        }
    }

    public void resourceRejected(OtlpTransport transport, OtlpMetricResourceRejectReason reason, long points) {
        if (points > 0) {
            resourceRejected.get(transport).get(reason).increment(points);
        }
    }

    public void statStored(OtlpTransport transport, int count) {
        if (count > 0) {
            statStored.get(transport).increment(count);
        }
    }

    /** One of the {@code START_TIME_*} sources. */
    public void agentStartTime(OtlpTransport transport, String source) {
        final Counter counter = agentStartTime.get(transport).get(source);
        if (counter == null) {
            throw new IllegalArgumentException("unknown agent start time source " + source);
        }
        counter.increment();
    }

    public void agentStatInsertError() {
        agentStatInsertError.increment();
    }

    public void agentInfoInsertError() {
        agentInfoInsertError.increment();
    }

    @Override
    public void requestRejected(OtlpTransport transport, OtlpRequestRejectReason reason) {
        final Counter counter = requestRejected.get(transport).get(reason);
        if (counter == null) {
            throw new IllegalArgumentException("reason " + reason + " is not emitted by transport " + transport);
        }
        counter.increment();
    }

    @Override
    public void requestBytes(OtlpTransport transport, long bytes) {
        if (bytes > 0) {
            requestBytes.get(transport).record(bytes);
        }
    }

    @Override
    public void registerInFlightBytes(OtlpTransport transport, LongSupplier reservedBytes, long limitBytes) {
        Gauge.builder(ADMISSION_INFLIGHT_BYTES, reservedBytes::getAsLong)
                .description("Bytes currently reserved by the metrics in-flight admission semaphore")
                .baseUnit("bytes")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
        Gauge.builder(ADMISSION_LIMIT_BYTES, () -> limitBytes)
                .description("Metrics in-flight byte budget (otlptrace.inspector admission.max-in-flight-bytes)")
                .baseUnit("bytes")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
    }

    @Override
    public void registerInFlightRequests(OtlpTransport transport, IntSupplier inFlightRequests, int limitRequests) {
        Gauge.builder(ADMISSION_INFLIGHT_REQUESTS, inFlightRequests::getAsInt)
                .description("Metrics requests currently past the concurrency gate")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
        Gauge.builder(ADMISSION_LIMIT_REQUESTS, () -> limitRequests)
                .description("Metrics concurrent-request cap (otlptrace.inspector http.max-concurrent-requests)")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
    }
}
