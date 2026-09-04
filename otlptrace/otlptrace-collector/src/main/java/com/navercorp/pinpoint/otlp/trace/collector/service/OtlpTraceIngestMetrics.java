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
 *       mapping (admission / parse), where the span count is unknown, by {@link RequestRejectReason}.</li>
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
public class OtlpTraceIngestMetrics {

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

    public enum Transport {
        GRPC("grpc"),
        HTTP("http");

        private final String tagValue;

        Transport(String tagValue) {
            this.tagValue = tagValue;
        }

        public String tagValue() {
            return tagValue;
        }
    }

    /**
     * Why a whole export request was refused before its spans were parsed or mapped. The response
     * the client sees is listed per reason; every one of them is retryable except the client faults
     * ({@code payload_too_large}, {@code unsupported_encoding}, {@code parse_error}).
     */
    public enum RequestRejectReason {
        /** gRPC / HTTP: the in-flight byte budget is exhausted (gRPC UNAVAILABLE, HTTP 503 + Retry-After). */
        INFLIGHT_BYTES("inflight_bytes"),
        /** gRPC: the worker executor queue is full (UNAVAILABLE). */
        EXECUTOR_REJECTED("executor_rejected"),
        /** HTTP: the concurrent-request cap is reached (503 + Retry-After). */
        CONCURRENCY("concurrency"),
        /** HTTP: Content-Length above the per-request cap (413). */
        PAYLOAD_TOO_LARGE("payload_too_large"),
        /** HTTP: Content-Encoding other than gzip/identity (415). */
        UNSUPPORTED_ENCODING("unsupported_encoding"),
        /** HTTP: the protobuf/JSON body did not parse (400). */
        PARSE_ERROR("parse_error");

        private final String tagValue;

        RequestRejectReason(String tagValue) {
            this.tagValue = tagValue;
        }

        public String tagValue() {
            return tagValue;
        }
    }

    private static final Set<RequestRejectReason> GRPC_REQUEST_REASONS =
            EnumSet.of(RequestRejectReason.INFLIGHT_BYTES, RequestRejectReason.EXECUTOR_REJECTED);
    private static final Set<RequestRejectReason> HTTP_REQUEST_REASONS =
            EnumSet.of(RequestRejectReason.INFLIGHT_BYTES, RequestRejectReason.CONCURRENCY,
                    RequestRejectReason.PAYLOAD_TOO_LARGE, RequestRejectReason.UNSUPPORTED_ENCODING,
                    RequestRejectReason.PARSE_ERROR);

    private final Map<Transport, Counter> received = new EnumMap<>(Transport.class);
    private final Map<Transport, Counter> storedSpan = new EnumMap<>(Transport.class);
    private final Map<Transport, Counter> storedSpanChunk = new EnumMap<>(Transport.class);
    private final Map<Transport, Map<OtlpTraceRejectReason, Counter>> spanRejected = new EnumMap<>(Transport.class);
    private final Map<Transport, Map<RequestRejectReason, Counter>> requestRejected = new EnumMap<>(Transport.class);
    private final Map<Transport, DistributionSummary> requestBytes = new EnumMap<>(Transport.class);
    private final MeterRegistry meterRegistry;

    public OtlpTraceIngestMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry");
        for (Transport transport : Transport.values()) {
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

            final Map<RequestRejectReason, Counter> requestByReason = new EnumMap<>(RequestRejectReason.class);
            final Set<RequestRejectReason> reasons = transport == Transport.GRPC ? GRPC_REQUEST_REASONS : HTTP_REQUEST_REASONS;
            for (RequestRejectReason reason : reasons) {
                requestByReason.put(reason, Counter.builder(REQUEST_REJECTED)
                        .description("OTLP export requests refused before mapping (admission or parse failure)")
                        .tag(TAG_TRANSPORT, transport.tagValue())
                        .tag(TAG_REASON, reason.tagValue())
                        .register(meterRegistry));
            }
            requestRejected.put(transport, requestByReason);
        }
    }

    private static Counter storedCounter(MeterRegistry meterRegistry, Transport transport, String type) {
        return Counter.builder(SPAN_STORED)
                .description("OTLP root spans / span chunks handed to storage after mapping")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .tag(TAG_TYPE, type)
                .register(meterRegistry);
    }

    /** Wire size of one admitted request (recorded after the admission gates, before mapping). */
    public void requestBytes(Transport transport, long bytes) {
        if (bytes > 0) {
            requestBytes.get(transport).record(bytes);
        }
    }

    /**
     * Exposes an in-flight byte admission gate as two gauges: bytes currently reserved and the
     * budget. Registered by the owner of the semaphore (gRPC service / HTTP filter) at construction;
     * the supplier is held strongly so the gauge never goes NaN through garbage collection.
     */
    public void registerInFlightBytes(Transport transport, LongSupplier reservedBytes, long limitBytes) {
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
    public void registerInFlightRequests(Transport transport, IntSupplier inFlightRequests, int limitRequests) {
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

    public void spanReceived(Transport transport, int count) {
        if (count > 0) {
            received.get(transport).increment(count);
        }
    }

    public void spanStored(Transport transport, int count) {
        if (count > 0) {
            storedSpan.get(transport).increment(count);
        }
    }

    public void spanChunkStored(Transport transport, int count) {
        if (count > 0) {
            storedSpanChunk.get(transport).increment(count);
        }
    }

    public void spanRejected(Transport transport, OtlpTraceRejectReason reason, long count) {
        if (count > 0) {
            spanRejected.get(transport).get(reason).increment(count);
        }
    }

    /**
     * Counts one refused request. The (transport, reason) pair must be one that transport can emit;
     * an unknown pair is a programming error and fails fast.
     */
    public void requestRejected(Transport transport, RequestRejectReason reason) {
        final Counter counter = requestRejected.get(transport).get(reason);
        if (counter == null) {
            throw new IllegalArgumentException("reason " + reason + " is not emitted by transport " + transport);
        }
        counter.increment();
    }
}
