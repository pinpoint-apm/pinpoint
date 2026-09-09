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

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/**
 * The slice of the ingest metrics the transport-level admission code needs: request-level
 * rejections, admitted wire bytes and the admission-gate gauges. The HTTP admission and
 * decompression filters depend on this interface rather than on {@link OtlpTraceIngestMetrics}, so
 * the same filter classes can front another OTLP signal's endpoint (e.g. {@code /v1/logs}) with
 * that signal's own metrics and budgets. The value types it takes ({@link OtlpTransport},
 * {@link OtlpRequestRejectReason}) are signal-neutral on purpose, so implementing it does not pull
 * in the trace metrics class.
 */
public interface OtlpIngestAdmissionMetrics {

    /** Counts one refused request; the (transport, reason) pair must be one the transport can emit. */
    void requestRejected(OtlpTransport transport, OtlpRequestRejectReason reason);

    /** Wire size of one admitted request. */
    void requestBytes(OtlpTransport transport, long bytes);

    /** Registers the in-flight byte gate as gauges (reserved bytes and the budget). */
    void registerInFlightBytes(OtlpTransport transport, LongSupplier reservedBytes, long limitBytes);

    /** Registers the concurrent-request gate as gauges (requests past the gate and the cap). */
    void registerInFlightRequests(OtlpTransport transport, IntSupplier inFlightRequests, int limitRequests);
}
