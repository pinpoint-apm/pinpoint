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

import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorRejectedSpan;

/**
 * Transport-agnostic outcome of an OTLP trace export.
 * <p>
 * Splits the two failure classes so each transport can map them to its own response semantics:
 * <ul>
 *     <li>{@code serverErrorCount} / {@code serverMessage} — server-side/transient failures
 *     (HBase insert, agentInfo). Retryable: gRPC returns UNAVAILABLE, HTTP should return 5xx.</li>
 *     <li>{@code clientRejected} — deterministic client-side data faults (invalid/unlinkable spans).
 *     Reported via OTLP partial success rather than a retryable error.</li>
 * </ul>
 */
public record OtlpTraceExportResult(OtlpTraceCollectorRejectedSpan clientRejected,
                                    int serverErrorCount,
                                    String serverMessage) {
}
