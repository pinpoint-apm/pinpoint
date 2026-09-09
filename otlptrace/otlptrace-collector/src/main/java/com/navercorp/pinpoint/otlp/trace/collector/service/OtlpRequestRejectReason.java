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

/**
 * Why a whole OTLP export request was refused before its payload was parsed or mapped.
 * Signal-neutral: every reason is a transport-level (admission, encoding, parse) fault, so the same
 * tag values serve traces and logs alike. The response the client sees is listed per reason; every
 * one of them is retryable except the client faults ({@code payload_too_large},
 * {@code unsupported_encoding}, {@code parse_error}).
 */
public enum OtlpRequestRejectReason {
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

    OtlpRequestRejectReason(String tagValue) {
        this.tagValue = tagValue;
    }

    public String tagValue() {
        return tagValue;
    }
}
