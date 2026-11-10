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

import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsPartialSuccess;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;

/**
 * Maps an {@link OtlpMetricExportResult} to the OTLP response shared by the gRPC and HTTP
 * transports. Only rejected Resources become {@code rejected_data_points}; metrics the receiver
 * does not map (application metrics, unsupported types) are a clean success from the exporter's
 * point of view, per the OTLP rule that a server may drop data it does not want without reporting
 * it as rejected.
 */
public final class OtlpMetricResponseMapper {

    private OtlpMetricResponseMapper() {
    }

    public static ExportMetricsServiceResponse toResponse(OtlpMetricExportResult result) {
        final long rejected = result.getRejectedPointCount();
        if (rejected > 0) {
            final ExportMetricsPartialSuccess partialSuccess = ExportMetricsPartialSuccess.newBuilder()
                    .setRejectedDataPoints(rejected)
                    .setErrorMessage(result.getRejectedMessage())
                    .build();
            return ExportMetricsServiceResponse.newBuilder().setPartialSuccess(partialSuccess).build();
        }
        return ExportMetricsServiceResponse.getDefaultInstance();
    }
}
