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

package com.navercorp.pinpoint.otlp.trace.inspector;

import io.opentelemetry.proto.metrics.v1.AggregationTemporality;

/**
 * Aggregation temporality of a Sum / Histogram point. Gauges have none. OTel SDKs default to
 * CUMULATIVE; DELTA is an exporter preference ({@code OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE}).
 */
public enum Temporality {
    UNSPECIFIED,
    CUMULATIVE,
    DELTA;

    static Temporality of(AggregationTemporality temporality) {
        if (temporality == AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE) {
            return CUMULATIVE;
        }
        if (temporality == AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA) {
            return DELTA;
        }
        return UNSPECIFIED;
    }
}
