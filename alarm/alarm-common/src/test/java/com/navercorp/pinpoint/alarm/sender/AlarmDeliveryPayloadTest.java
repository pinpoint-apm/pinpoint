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
package com.navercorp.pinpoint.alarm.sender;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlarmDeliveryPayloadTest {

    private final JsonMapper jsonMapper = new JsonMapper();

    @Test
    void emailSnapshot_roundTripsOnlyEmailFields() {
        AlarmDeliveryPayload payload = AlarmDeliveryPayload.forEmail(
                "subject", "<p>body</p>", List.of("user@example.test"));

        assertRoundTrip(payload, Map.of(
                "subject", "subject",
                "content", "<p>body</p>",
                "destinations", List.of("user@example.test")));
    }

    @Test
    void smsSnapshot_roundTripsOnlyFinalContentAndRecipients() {
        AlarmDeliveryPayload payload = AlarmDeliveryPayload.forSms(
                "title\nbody", List.of("01012345678"));

        assertRoundTrip(payload, Map.of(
                "content", "title\nbody",
                "destinations", List.of("01012345678")));
    }

    @Test
    void webhookSnapshot_roundTripsFinalRequestBodyWithoutFormat() {
        AlarmDeliveryPayload payload = AlarmDeliveryPayload.forWebhook(
                "{\"text\":\"title\\nbody\"}", "https://example.test/hook");

        assertRoundTrip(payload, Map.of(
                "content", "{\"text\":\"title\\nbody\"}",
                "destinations", List.of("https://example.test/hook")));
    }

    @Test
    void validateFor_rejectsIncompleteTransportPayloads() {
        assertAll(
                () -> assertThrows(AlarmSendException.class,
                        () -> new AlarmDeliveryPayload(null, "content", List.of("a@example.test"))
                                .validateFor(AlarmMethodType.EMAIL)),
                () -> assertThrows(AlarmSendException.class,
                        () -> new AlarmDeliveryPayload("subject", "content", null)
                                .validateFor(AlarmMethodType.EMAIL)),
                () -> assertThrows(AlarmSendException.class,
                        () -> new AlarmDeliveryPayload(null, null, List.of("01012345678"))
                                .validateFor(AlarmMethodType.SMS)),
                () -> assertThrows(AlarmSendException.class,
                        () -> new AlarmDeliveryPayload(null, "content", null)
                                .validateFor(AlarmMethodType.SMS)),
                () -> assertThrows(AlarmSendException.class,
                        () -> new AlarmDeliveryPayload(null, "{}", List.of())
                                .validateFor(AlarmMethodType.WEBHOOK)),
                () -> assertThrows(AlarmSendException.class,
                        () -> new AlarmDeliveryPayload(
                                null, "{}", List.of("https://a.test", "https://b.test"))
                                .validateFor(AlarmMethodType.WEBHOOK))
        );
    }

    private void assertRoundTrip(AlarmDeliveryPayload payload, Map<String, Object> expected) {
        String serialized = jsonMapper.writeValueAsString(payload);
        JsonNode actual = jsonMapper.readTree(serialized);

        assertEquals(jsonMapper.valueToTree(expected), actual);
        assertEquals(payload, jsonMapper.readValue(serialized, AlarmDeliveryPayload.class));
    }
}
