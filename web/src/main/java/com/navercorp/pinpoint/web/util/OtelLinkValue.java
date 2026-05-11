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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import org.jspecify.annotations.Nullable;

/**
 * Typed representation of an {@code OPENTELEMETRY_LINK} annotation value.
 *
 * <p>The annotation is persisted as a JSON string for compatibility with the existing
 * HBase wire format. This record centralizes parsing and serialization so that
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static @Nullable OtelLinkValue parse(@Nullable Object annotationValue) {
        if (!(annotationValue instanceof String json) || json.isEmpty()) {
            return null;
        }
        try {
            final JsonNode root = OBJECT_MAPPER.readTree(json);
            if (!root.isObject()) {
                return null;
            }
            return new OtelLinkValue(
                    readTraceId(root.path("traceId")),
                    readLong(root.path("spanId")),
                    readTraceId(root.path("linkTraceId")),
                    readLong(root.path("linkSpanId")),
                    readLong(root.path("focusTimestamp")),
                    extractNode(root.get("attributes"))
            );
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public OtelLinkValue withDownstream(@Nullable ServerTraceId linkTraceId, long linkSpanId, long focusTimestamp) {
        return new OtelLinkValue(this.traceId, this.spanId, linkTraceId, linkSpanId, focusTimestamp, this.attributes);
    }

    public String toJson() {
        final ObjectNode node = OBJECT_MAPPER.createObjectNode();
        if (traceId != null) {
            node.put("traceId", traceId.toString());
        }
        // long values serialized as String to avoid JS Number precision loss on the frontend
        if (spanId != null) {
            node.put("spanId", String.valueOf(spanId));
        }
        if (linkTraceId != null) {
            node.put("linkTraceId", linkTraceId.toString());
        }
        if (linkSpanId != null) {
            node.put("linkSpanId", String.valueOf(linkSpanId));
        }
        if (focusTimestamp != null) {
            node.put("focusTimestamp", focusTimestamp.longValue());
        }
        if (attributes != null) {
            node.set("attributes", attributes);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private static @Nullable ServerTraceId readTraceId(@Nullable JsonNode node) {
        final String text = readText(node);
        if (text == null) {
            return null;
        }
        try {
            return ServerTraceId.of(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static @Nullable String readText(@Nullable JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || !node.isTextual()) {
            return null;
        }
        final String text = node.asText();
        return text.isEmpty() ? null : text;
    }

    private static @Nullable Long readLong(@Nullable JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asLong();
        }
        if (node.isTextual()) {
            try {
                return Long.parseLong(node.asText());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static @Nullable JsonNode extractNode(@Nullable JsonNode node) {
        return (node != null && !node.isMissingNode() && !node.isNull()) ? node : null;
    }
}
