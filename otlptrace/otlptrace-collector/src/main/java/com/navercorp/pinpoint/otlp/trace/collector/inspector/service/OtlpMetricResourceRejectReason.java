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

/**
 * Why a whole {@code ResourceMetrics} was rejected before mapping. Both are client-side data
 * faults and are reported back in the OTLP partial success (as {@code rejected_data_points}), the
 * same policy as the trace and log paths. Point-level drops inside an accepted Resource (unknown
 * metric names, unsupported types, missing baseline) are the receiver's own selection and are only
 * counted ({@code collector.otlptrace.inspector.point.dropped}).
 */
public enum OtlpMetricResourceRejectReason {
    /** No usable application/agent identifier, or one that fails validation. */
    INVALID_RESOURCE("invalid_resource", "invalid resource"),
    /** {@code pinpoint.serviceName} names a service that is not registered. */
    SERVICE_NOT_FOUND("service_not_found", "service not found");

    private final String tagValue;
    private final String message;

    OtlpMetricResourceRejectReason(String tagValue, String message) {
        this.tagValue = tagValue;
        this.message = message;
    }

    /** Metric tag value. */
    public String tagValue() {
        return tagValue;
    }

    /** Label in the partial-success error message, e.g. {@code invalid resource (12)}. */
    public String message() {
        return message;
    }
}
