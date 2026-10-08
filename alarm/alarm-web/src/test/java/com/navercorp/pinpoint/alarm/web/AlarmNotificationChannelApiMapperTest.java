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
package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmNotificationChannelApiMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AlarmNotificationChannelApiMapper mapper =
            new AlarmNotificationChannelApiMapper(objectMapper);

    @Test
    void convertsApiConfigObjectToDatabaseJsonString() {
        AlarmNotificationChannelRequest request = new AlarmNotificationChannelRequest();
        ObjectNode config = objectMapper.createObjectNode();
        config.put("format", "SLACK");
        config.put("title", "[${severity}] ${name}");
        request.setConfig(config);

        AlarmNotificationChannel channel = mapper.toModel("DEFAULT", request);

        assertEquals(
                "{\"format\":\"SLACK\",\"title\":\"[${severity}] ${name}\"}",
                channel.getConfig());
    }

    @Test
    void convertsDatabaseJsonStringToApiConfigObject() {
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setConfig("{\"format\":\"SLACK\",\"futureField\":true}");

        AlarmNotificationChannelResponse response = mapper.toResponse(channel);

        assertEquals("SLACK", response.config().get("format").textValue());
        assertTrue(response.config().get("futureField").booleanValue());
    }

    @Test
    void mapsEachCountToItsOwnField() {
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setTemplateCount(1);
        channel.setAffectedRuleCount(2);
        channel.setEnabledAffectedRuleCount(3);
        channel.setRecipientCount(4);

        AlarmNotificationChannelResponse response = mapper.toResponse(channel);

        // Four int components in a row: distinct values catch a swapped constructor argument,
        // which otherwise compiles and leaves recipientCount carrying someone else's number.
        assertEquals(1, response.templateCount());
        assertEquals(2, response.affectedRuleCount());
        assertEquals(3, response.enabledAffectedRuleCount());
        assertEquals(4, response.recipientCount());
    }

    @Test
    void omitsRecipientCountWhenTheQueryDidNotComputeIt() throws Exception {
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setMethodType(AlarmMethodType.EMAIL);

        String json = objectMapper.writeValueAsString(mapper.toResponse(channel));

        // 0 marks a channel that reaches nobody, so a count that was never read must not
        // serialize as one.
        assertFalse(json.contains("recipientCount"));
    }

    @Test
    void rejectsNonObjectApiConfig() {
        AlarmNotificationChannelRequest request = new AlarmNotificationChannelRequest();
        request.setConfig(objectMapper.createArrayNode());

        assertThrows(IllegalArgumentException.class, () -> mapper.toModel("DEFAULT", request));
    }
}
