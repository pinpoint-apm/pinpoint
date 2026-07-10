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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import org.apache.commons.io.output.StringBuilderWriter;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Serializes and parses the {@code OPENTELEMETRY_LINK} annotation value, which is persisted
 * as a JSON string for compatibility with the existing HBase wire format, to and from a
 * typed {@link OtelLinkValue}.
 */
public final class OtelLinkValueSerde {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private OtelLinkValueSerde() {
    }

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
        if (JsonNodeUtils.isNull(node) || !node.isTextual()) {
            return null;
        }
        final String text = node.asText();
        return text.isEmpty() ? null : text;
    }

    private static @Nullable Long readLong(@Nullable JsonNode node) {
        if (JsonNodeUtils.isNull(node)) {
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
        return JsonNodeUtils.isNull(node) ? null : node;
    }

    public static String toJson(OtelLinkValue value) {
        final StringBuilderWriter writer = new StringBuilderWriter();
        try (JsonGenerator gen = OBJECT_MAPPER.createGenerator(writer)) {
            gen.writeStartObject();
            if (value.traceId() != null) {
                gen.writeStringField("traceId", value.traceId().toString());
            }
            // long values serialized as String to avoid JS Number precision loss on the frontend
            if (value.spanId() != null) {
                gen.writeStringField("spanId", String.valueOf(value.spanId()));
            }
            if (value.linkTraceId() != null) {
                gen.writeStringField("linkTraceId", value.linkTraceId().toString());
            }
            if (value.linkSpanId() != null) {
                gen.writeStringField("linkSpanId", String.valueOf(value.linkSpanId()));
            }
            if (value.focusTimestamp() != null) {
                gen.writeNumberField("focusTimestamp", value.focusTimestamp());
            }
            if (value.attributes() != null) {
                gen.writeFieldName("attributes");
                gen.writeTree(value.attributes());
            }
            gen.writeEndObject();
        } catch (IOException e) {
            return "{}";
        }
        return writer.toString();
    }
}
