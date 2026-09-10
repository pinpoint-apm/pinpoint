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

import com.navercorp.pinpoint.otlp.log.collector.OtlpLogRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpIngestAdmissionMetrics;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpRequestRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
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
 * Ingest volume of the OTLP logs receiver, per transport, in the {@code collector.otlplog.*} name
 * space so it never mixes with the trace collector's {@code collector.otlptrace.*} series:
 * <ul>
 *     <li>{@code record.received / stored / dropped{reason}} — LogRecord-level: every record that
 *     arrived, the ones handed to Error Analysis storage, and the rest by
 *     {@link OtlpLogRejectReason}. {@code received - stored} is dominated by {@code no_exception}
 *     (ordinary log lines) and is the log volume indicator; {@code duplicate} shows the in-request
 *     de-duplication effect.</li>
 *     <li>{@code record.unsampled_context} — stored records whose trace flags said the trace was
 *     not sampled, so no span exists to link them to (only when storing them is enabled).</li>
 *     <li>{@code request.rejected{reason} / request.bytes / admission.*} — the transport-level
 *     admission slice ({@link OtlpIngestAdmissionMetrics}), same meaning as the trace path's.</li>
 *     <li>{@code store.error} — synchronous failures handing a record to the exceptiontrace store.</li>
 * </ul>
 * Every (transport, reason) series is registered up front so it exists at zero before the first
 * event; the hot path only increments pre-built counters.
 */
@Component
public class OtlpLogIngestMetrics implements OtlpIngestAdmissionMetrics {

    public static final String RECORD_RECEIVED = "collector.otlplog.record.received";
    public static final String RECORD_STORED = "collector.otlplog.record.stored";
    public static final String RECORD_DROPPED = "collector.otlplog.record.dropped";
    public static final String RECORD_UNSAMPLED_CONTEXT = "collector.otlplog.record.unsampled_context";
    public static final String REQUEST_REJECTED = "collector.otlplog.request.rejected";
    public static final String REQUEST_BYTES = "collector.otlplog.request.bytes";
    public static final String ADMISSION_INFLIGHT_BYTES = "collector.otlplog.admission.inflight.bytes";
    public static final String ADMISSION_LIMIT_BYTES = "collector.otlplog.admission.limit.bytes";
    public static final String ADMISSION_INFLIGHT_REQUESTS = "collector.otlplog.admission.inflight.requests";
    public static final String ADMISSION_LIMIT_REQUESTS = "collector.otlplog.admission.limit.requests";
    public static final String STORE_ERROR = "collector.otlplog.store.error";

    public static final String TAG_TRANSPORT = "transport";
    public static final String TAG_REASON = "reason";

    private static final Set<OtlpRequestRejectReason> GRPC_REQUEST_REASONS =
            EnumSet.of(OtlpRequestRejectReason.INFLIGHT_BYTES, OtlpRequestRejectReason.EXECUTOR_REJECTED);
    private static final Set<OtlpRequestRejectReason> HTTP_REQUEST_REASONS =
            EnumSet.of(OtlpRequestRejectReason.INFLIGHT_BYTES, OtlpRequestRejectReason.CONCURRENCY,
                    OtlpRequestRejectReason.PAYLOAD_TOO_LARGE, OtlpRequestRejectReason.UNSUPPORTED_ENCODING,
                    OtlpRequestRejectReason.PARSE_ERROR);

    private final Map<OtlpTransport, Counter> received = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Counter> stored = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Counter> unsampledContext = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Map<OtlpLogRejectReason, Counter>> dropped = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, Map<OtlpRequestRejectReason, Counter>> requestRejected = new EnumMap<>(OtlpTransport.class);
    private final Map<OtlpTransport, DistributionSummary> requestBytes = new EnumMap<>(OtlpTransport.class);
    private final Counter storeError;
    private final MeterRegistry meterRegistry;

    public OtlpLogIngestMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry");
        for (OtlpTransport transport : OtlpTransport.values()) {
            received.put(transport, Counter.builder(RECORD_RECEIVED)
                    .description("OTLP LogRecords carried by admitted export requests, before selection")
                    .tag(TAG_TRANSPORT, transport.tagValue())
                    .register(meterRegistry));
            stored.put(transport, Counter.builder(RECORD_STORED)
                    .description("Exception LogRecords handed to Error Analysis storage")
                    .tag(TAG_TRANSPORT, transport.tagValue())
                    .register(meterRegistry));
            unsampledContext.put(transport, Counter.builder(RECORD_UNSAMPLED_CONTEXT)
                    .description("Stored exception LogRecords whose trace was not sampled (no span to link to)")
                    .tag(TAG_TRANSPORT, transport.tagValue())
                    .register(meterRegistry));

            final Map<OtlpLogRejectReason, Counter> droppedByReason = new EnumMap<>(OtlpLogRejectReason.class);
            for (OtlpLogRejectReason reason : OtlpLogRejectReason.values()) {
                droppedByReason.put(reason, Counter.builder(RECORD_DROPPED)
                        .description("OTLP LogRecords not stored, by reason (most are non-exception logs dropped by design)")
                        .tag(TAG_TRANSPORT, transport.tagValue())
                        .tag(TAG_REASON, reason.tagValue())
                        .register(meterRegistry));
            }
            dropped.put(transport, droppedByReason);

            final Map<OtlpRequestRejectReason, Counter> requestByReason = new EnumMap<>(OtlpRequestRejectReason.class);
            final Set<OtlpRequestRejectReason> reasons = transport == OtlpTransport.GRPC ? GRPC_REQUEST_REASONS : HTTP_REQUEST_REASONS;
            for (OtlpRequestRejectReason reason : reasons) {
                requestByReason.put(reason, Counter.builder(REQUEST_REJECTED)
                        .description("OTLP logs export requests refused before selection (admission or parse failure)")
                        .tag(TAG_TRANSPORT, transport.tagValue())
                        .tag(TAG_REASON, reason.tagValue())
                        .register(meterRegistry));
            }
            requestRejected.put(transport, requestByReason);

            requestBytes.put(transport, DistributionSummary.builder(REQUEST_BYTES)
                    .description("Wire bytes of admitted OTLP logs export requests (gRPC serialized size / HTTP body)")
                    .baseUnit("bytes")
                    .tag(TAG_TRANSPORT, transport.tagValue())
                    .register(meterRegistry));
        }
        this.storeError = Counter.builder(STORE_ERROR)
                .description("Synchronous failures handing an exception LogRecord to the exceptiontrace store")
                .register(meterRegistry);
    }

    public void recordReceived(OtlpTransport transport, int count) {
        if (count > 0) {
            received.get(transport).increment(count);
        }
    }

    public void recordStored(OtlpTransport transport, int count) {
        if (count > 0) {
            stored.get(transport).increment(count);
        }
    }

    public void recordUnsampledContext(OtlpTransport transport, int count) {
        if (count > 0) {
            unsampledContext.get(transport).increment(count);
        }
    }

    public void recordDropped(OtlpTransport transport, OtlpLogRejectReason reason, long count) {
        if (count > 0) {
            dropped.get(transport).get(reason).increment(count);
        }
    }

    public void storeError() {
        storeError.increment();
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
                .description("Bytes currently reserved by the logs in-flight admission semaphore")
                .baseUnit("bytes")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
        Gauge.builder(ADMISSION_LIMIT_BYTES, () -> limitBytes)
                .description("Logs in-flight byte budget (otlplog admission.max-in-flight-bytes)")
                .baseUnit("bytes")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
    }

    @Override
    public void registerInFlightRequests(OtlpTransport transport, IntSupplier inFlightRequests, int limitRequests) {
        Gauge.builder(ADMISSION_INFLIGHT_REQUESTS, inFlightRequests::getAsInt)
                .description("Logs requests currently past the concurrency gate")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
        Gauge.builder(ADMISSION_LIMIT_REQUESTS, () -> limitRequests)
                .description("Logs concurrent-request cap (otlplog http.max-concurrent-requests)")
                .tag(TAG_TRANSPORT, transport.tagValue())
                .strongReference(true)
                .register(meterRegistry);
    }
}
