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
package com.navercorp.pinpoint.web.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import org.jspecify.annotations.Nullable;

/**
 * Typed representation of an {@code OPENTELEMETRY_LINK} annotation value.
 *
 * <p>The annotation is persisted as a JSON string for compatibility with the existing
 * HBase wire format. {@link OtelLinkValueSerde} centralizes parsing and serialization so that
 * consumers can operate on typed fields instead of poking at JSON nodes.</p>
 *
 * <p>Fields mirror the JSON keys produced by the collector (raw) and by
 * {@code RecordFactory#augmentOtelLink} (augmented for the frontend):</p>
 * <ul>
 *   <li>{@code traceId} / {@code spanId} - upstream OTel link target</li>
 *   <li>{@code linkTraceId} / {@code linkSpanId} - downstream span where the Link annotation lives</li>
 *   <li>{@code focusTimestamp} - downstream collector accept time</li>
 *   <li>{@code attributes} - opaque OTel Link attributes</li>
 * </ul>
 */
public record OtelLinkValue(
        @Nullable ServerTraceId traceId,
        @Nullable Long spanId,
        @Nullable ServerTraceId linkTraceId,
        @Nullable Long linkSpanId,
        @Nullable Long focusTimestamp,
        @Nullable JsonNode attributes
) {

    public OtelLinkValue withDownstream(@Nullable ServerTraceId linkTraceId, long linkSpanId, long focusTimestamp) {
        return new OtelLinkValue(this.traceId, this.spanId, linkTraceId, linkSpanId, focusTimestamp, this.attributes);
    }
}
