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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OtelLinkValueSerdeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void roundTrip_allFields() {
        ObjectNode attributes = mapper.createObjectNode();
        attributes.put("http.method", "GET");
        attributes.put("retry", 3);

        OtelLinkValue original = new OtelLinkValue(
                new PinpointServerTraceId("upstream-agent", 100, 200),
                1234567890123456789L,
                new PinpointServerTraceId("downstream-agent", 300, 400),
                987654321098765432L,
                1670305848569L,
                attributes
        );

        OtelLinkValue parsed = OtelLinkValueSerde.parse(OtelLinkValueSerde.toJson(original));

        assertThat(parsed).isEqualTo(original);
    }

    @Test
    void roundTrip_otelTraceId() {
        // 32 hex chars (16 bytes) without the pinpoint delimiter -> OtelServerTraceId
        ServerTraceId otelTraceId = ServerTraceId.of("0123456789abcdef0123456789abcdef");

        OtelLinkValue original = new OtelLinkValue(otelTraceId, 42L, null, null, null, null);

        OtelLinkValue parsed = OtelLinkValueSerde.parse(OtelLinkValueSerde.toJson(original));

        assertThat(parsed).isEqualTo(original);
    }

    @Test
    void roundTrip_nullFieldsOmitted() {
        // Only the upstream target is present; downstream/attributes are null.
        OtelLinkValue original = new OtelLinkValue(
                new PinpointServerTraceId("upstream-agent", 100, 200),
                1L,
                null,
                null,
                null,
                null
        );

        String json = OtelLinkValueSerde.toJson(original);

        assertThat(json).doesNotContain("linkTraceId", "linkSpanId", "focusTimestamp", "attributes");
        assertThat(OtelLinkValueSerde.parse(json)).isEqualTo(original);
    }

    @Test
    void toJson_wireFormat_longsAsString_timestampAsNumber() throws Exception {
        OtelLinkValue value = new OtelLinkValue(
                new PinpointServerTraceId("agent", 1, 2),
                111L,
                new PinpointServerTraceId("agent", 3, 4),
                222L,
                1670305848569L,
                null
        );

        JsonNode node = mapper.readTree(OtelLinkValueSerde.toJson(value));

        // long ids are serialized as JSON strings to avoid JS Number precision loss on the frontend
        assertThat(node.get("spanId").isTextual()).isTrue();
        assertThat(node.get("spanId").asText()).isEqualTo("111");
        assertThat(node.get("linkSpanId").isTextual()).isTrue();
        // focusTimestamp stays a JSON number
        assertThat(node.get("focusTimestamp").isNumber()).isTrue();
        assertThat(node.get("focusTimestamp").asLong()).isEqualTo(1670305848569L);
    }

    @Test
    void parse_invalidInput_returnsNull() {
        assertThat(OtelLinkValueSerde.parse(null)).isNull();
        assertThat(OtelLinkValueSerde.parse("")).isNull();
        assertThat(OtelLinkValueSerde.parse(123)).isNull();
        assertThat(OtelLinkValueSerde.parse("not json")).isNull();
        // valid JSON but not an object
        assertThat(OtelLinkValueSerde.parse("[1,2,3]")).isNull();
    }
}
