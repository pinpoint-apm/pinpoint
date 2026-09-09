/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.trace.collector.service;

import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceRejectReason;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * Ingest-volume counters for the OTLP trace endpoints, shared by the gRPC and HTTP transports.
 * <p>
 * The pre-existing meters count <em>requests</em> ({@code grpc.server.requests.received},
 * {@code http.server.requests}) or <em>worker tasks</em> ({@code grpcOtlpTraceWorkerExecutor.*}, one
 * task per export call); an export call carries a batch of spans, so none of them yields spans per
 * second. These counters fill that gap at the span level and split the two rejection classes:
 * <ul>
 *   <li>{@code collector.otlptrace.span.received{transport}} — spans carried by accepted export
 *       requests, before mapping.</li>
 *   <li>{@code collector.otlptrace.span.stored{transport,type}} — root spans ({@code span}) and
 *       orphan sub-trees ({@code spanChunk}) handed to storage.</li>
 *   <li>{@code collector.otlptrace.span.rejected{transport,reason}} — spans dropped during mapping,
 *       by {@link OtlpTraceRejectReason}. These are also reported to the client as
 *       {@code ExportTracePartialSuccess}.</li>
 *   <li>{@code collector.otlptrace.request.rejected{transport,reason}} — whole requests refused before
 *       mapping (admission / parse), where the span count is unknown, by {@link OtlpRequestRejectReason}.</li>
 *   <li>{@code collector.otlptrace.request.bytes{transport}} — wire bytes of admitted requests
 *       (distribution: count / total / mean / max per step), so bytes per second, average batch size
 *       and headroom against the per-request cap are visible.</li>
 *   <li>{@code collector.otlptrace.admission.inflight.bytes{transport}} and
 *       {@code .admission.limit.bytes{transport}} — bytes currently reserved by the in-flight
 *       admission semaphore vs. its budget; {@code .admission.inflight.requests} /
 *       {@code .admission.limit.requests} likewise for the HTTP concurrency gate. Gauges are sampled
 *       per step, so sub-step spikes show up only through {@code request.rejected{inflight_bytes}}.</li>
 * </ul>
 * Every (transport, reason) combination is registered up front so the series exist at zero before
 * the first event; a dashboard repeating on {@code reason} then shows a stable panel set. Callers
 * only call {@link Counter#increment(double)} on the pre-built counters — no registry lookup on the
 * hot path. All counters live in the collector's {@link MeterRegistry}, so they reach whichever
 * exporter that registry is wired to (NPOT in the NAVER deployment) without extra configuration.
 */
@Component
public class OtlpTraceIngestMetrics implements OtlpIngestAdmissionMetrics {

    public static final String SPAN_RECEIVED = "collector.otlptrace.span.received";
    public static final String SPAN_STORED = "collector.otlptrace.span.stored";
    public static final String SPAN_REJECTED = "collector.otlptrace.span.rejected";
    public static final String REQUEST_REJECTED = "collector.otlptrace.request.rejected";
    public static final String REQUEST_BYTES = "collector.otlptrace.request.bytes";
    public static final String ADMISSION_INFLIGHT_BYTES = "collector.otlptrace.admission.inflight.bytes";
    public static final String ADMISSION_LIMIT_BYTES = "collector.otlptrace.admission.limit.bytes";
    public static final String ADMISSION_INFLIGHT_REQUESTS = "collector.otlptrace.admission.inflight.requests";
    public static final String ADMISSION_LIMIT_REQUESTS = "collector.otlptrace.admission.limit.requests";

    public static final String TAG_TRANSPORT = "transport";
    public static final String TAG_TYPE = "type";
    public static final String TAG_REASON = "reason";

    public static final String TYPE_SPAN = "span";
    public static final String TYPE_SPAN_CHUNK = "spanChunk";

    private static final Set<OtlpRequestRejectReason> GRPC_REQUEST_REASONS =
            EnumSet.of(OtlpRequestRejectReason.INFLIGHT_BYTES, OtlpRequestRejectReason.EXECUTOR_REJECTED);
    private static final Set<OtlpRequestRejectReason> HTTP_REQUEST_REASONS =
            EnumSet.of(OtlpRequestRejectReason.INFLIGHT_BYTES, OtlpRequestRejectReason.CONCURRENCY,
                    OtlpRequestRejectReason.PAYLOAD_TOO_LARGE, OtlpRequestRejectReason.UNSUPPORTED_ENCODING,
                    OtlpRequestRejectReason.PARSE_ERROR);

    private final Map<OtlpTransport, Counter> received = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Counter> storedSpan = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Counter> storedSpanChunk = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Map<OtlpTraceRejectReason, Counter>> spanRejected = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Map<OtlpRequestRejectReason, Counter>> requestRejected = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, DistributionSummary> requestBytes = new EnumMap<>(OtlpTransport.class);
    private final MeterRegistry meterRegistry;

    public OtlpTraceIngestMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry");
        for (OtlpTransport transport : OtlpTransport.values()) {
            requestBytes.put(transport, DistributionSummary.builder(REQUEST_BYTES)
                    .description("Wire bytes of admitted OTLP export requests (gRPC serialized size / HTTP body)")
                    .baseUnit("bytes")
                    .tag(TAG_TRANSPORT, transport.tagValue())
                    .register(meterRegistry));
            received.put(transport, Counter.builder(SPAN_RECEIVED)
                    .description("OTLP spans carried by accepted export requests, before mapping")
                    .tag(TAG_TRANSPORT, transport.tagValue())
                    .register(meterRegistry));
            storedSpan.put(transport, storedCounter(meterRegistry, transport, TYPE_SPAN));
            storedSpanChunk.put(transport, storedCounter(meterRegistry, transport, TYPE_SPAN_CHUNK));

            final Map<OtlpTraceRejectReason, Counter> byReason = new EnumMap<>(OtlpTraceRejectReason.class);
            for (OtlpTraceRejectReason reason : OtlpTraceRejectReason.values()) {
                byReason.put(reason, Counter.builder(SPAN_REJECTED)
                        .description("OTLP spans rejected during mapping (client data fault, reported as partial success)")
                        .tag(TAG_TRANSPORT, transport.tagValue())
                        .tag(TAG_REASON, reason.tagValue())
                        .register(meterRegistry));
            }
            spanRejected.put(transport, byReason);

            final Map<OtlpRequestRejectReason, Counter> requestByReason = new EnumMap<>(OtlpRequestRejectReason.class);
            final Set<OtlpRequestRejectReason> reasons = transport == OtlpTransport.GRPC ? GRPC_REQUEST_REASONS : HTTP_REQUEST_REASONS;
            for (OtlpRequestRejectReason reason : reasons) {
                requestByReason.put(reason, Counter.builder(REQUEST_REJECTED)
                        .description("OTLP export requests refused before mapping (admission or parse failure)")
                        .tag(TAG_TRANSPORT, transport.tagValue())
                        .tag(TAG_REASON, reason.tagValue())
                        .register(meterRegistry));
            }
            requestRejected.put(transport, requestByReason);
        }
    }

    private static Counter storedCounter(MeterRegistry meterRegistry, OtlpTransport transport, String type) {
        return Counter.builder(SPAN_STORED)
                .description("OTLP root spans / span chunks handed to storage after mapping")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .tag(TAG_TYPE, type)
                .register(meterRegistry);
    }

    /** Wire size of one admitted request (recorded after the admission gates, before mapping). */
    @Override
    public void requestBytes(OtlpTransport transport, long bytes) {
        if (bytes > 0) {
            requestBytes.get(transport).record(bytes);
        }
    }

    /**
     * Exposes an in-flight byte admission gate as two gauges: bytes currently reserved and the
     * budget. Registered by the owner of the semaphore (gRPC service / HTTP filter) at construction;
     * the supplier is held strongly so the gauge never goes NaN through garbage collection.
     */
    @Override
    public void registerInFlightBytes(OtlpTransport transport, LongSupplier reservedBytes, long limitBytes) {
        Gauge.builder(ADMISSION_INFLIGHT_BYTES, reservedBytes::getAsLong)
                .description("Bytes currently reserved by the in-flight admission semaphore")
                .baseUnit("bytes")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
        Gauge.builder(ADMISSION_LIMIT_BYTES, () -> limitBytes)
                .description("In-flight byte budget (admission.max-in-flight-bytes)")
                .baseUnit("bytes")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
    }

    /** HTTP-only concurrent-request gate: requests currently admitted and the cap. */
    @Override
    public void registerInFlightRequests(OtlpTransport transport, IntSupplier inFlightRequests, int limitRequests) {
        Gauge.builder(ADMISSION_INFLIGHT_REQUESTS, inFlightRequests::getAsInt)
                .description("Requests currently past the concurrency gate")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
        Gauge.builder(ADMISSION_LIMIT_REQUESTS, () -> limitRequests)
                .description("Concurrent-request cap (http.max-concurrent-requests)")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
    }

    public void spanReceived(OtlpTransport transport, int count) {
        if (count > 0) {
            received.get(transport).increment(count);
        }
    }

    public void spanStored(OtlpTransport transport, int count) {
        if (count > 0) {
            storedSpan.get(transport).increment(count);
        }
    }

    public void spanChunkStored(OtlpTransport transport, int count) {
        if (count > 0) {
            storedSpanChunk.get(transport).increment(count);
        }
    }

    public void spanRejected(OtlpTransport transport, OtlpTraceRejectReason reason, long count) {
        if (count > 0) {
            spanRejected.get(transport).get(reason).increment(count);
        }
    }

    /**
     * Counts one refused request. The (transport, reason) pair must be one that transport can emit;
     * an unknown pair is a programming error and fails fast.
     */
    @Override
    public void requestRejected(OtlpTransport transport, OtlpRequestRejectReason reason) {
        final Counter counter = requestRejected.get(transport).get(reason);
        if (counter == null) {
            throw new IllegalArgumentException("reason " + reason + " is not emitted by transport " + transport);
        }
        counter.increment();
    }
}
