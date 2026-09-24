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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.vo.AlarmEventType;
import com.navercorp.pinpoint.alarm.vo.AlarmHistoryV2;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.alarm.util.json.UtcTimestampSerializer;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The columns hold a bare wall clock the schema writes in UTC. Sent on as one, a client reads it
 * as its own local time, which only looks right where the two agree -- so every timestamp the
 * alarm api answers with carries the zone it is in.
 *
 * <p>Run against a default zone that is not UTC, because on a UTC one this asserts nothing:
 * reading the stored value in the jvm's own zone gives the same answer, which is the mistake
 * being guarded against.
 */
class AlarmTimestampResponseTest {

    private static final LocalDateTime STORED = LocalDateTime.of(2026, 9, 21, 19, 50, 12);
    private static final String SENT = "2026-09-21T19:50:12Z";

    private static TimeZone defaultTimeZone;

    private final ObjectMapper objectMapper = Jackson.newMapper();

    @BeforeAll
    static void fixNonUtcDefaultZone() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    @AfterAll
    static void restoreDefaultZone() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    void ruleStateCarriesTheZoneItIsIn() {
        AlarmState state = new AlarmState(58L);
        state.setLastFiredAt(STORED);
        state.setNextCheckAt(STORED.plusMinutes(1));

        JsonNode json = objectMapper.valueToTree(state);

        assertEquals(SENT, json.get("lastFiredAt").asText());
        assertEquals("2026-09-21T19:51:12Z", json.get("nextCheckAt").asText());
    }

    @Test
    void ruleHistoryCarriesTheZoneItIsIn() {
        AlarmHistoryV2 history = new AlarmHistoryV2();
        history.setRuleId(58L);
        history.setEventType(AlarmEventType.FIRED);
        history.setCreatedAt(STORED);

        JsonNode json = objectMapper.valueToTree(history);

        assertEquals(SENT, json.get("createdAt").asText());
    }

    @Test
    void ruleCarriesTheZoneItIsIn() {
        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(58L);
        rule.setUpdatedAt(STORED);
        AlarmTemplateItem item = new AlarmTemplateItem();
        item.setUpdatedAt(STORED);

        JsonNode json = objectMapper.valueToTree(AlarmRuleResponse.from(rule, null, item));

        assertEquals(SENT, json.get("updatedAt").asText());
        // The nested item is the reason this is on the field rather than on a response of its own.
        assertEquals(SENT, json.get("templateItem").get("updatedAt").asText());
    }

    @Test
    void templateCarriesTheZoneItIsIn() {
        AlarmTemplate template = new AlarmTemplate();
        template.setUpdatedAt(STORED);
        AlarmTemplateItem item = new AlarmTemplateItem();
        item.setUpdatedAt(STORED);
        template.setItems(List.of(item));

        JsonNode json = objectMapper.valueToTree(template);

        assertEquals(SENT, json.get("updatedAt").asText());
        assertEquals(SENT, json.get("items").get(0).get("updatedAt").asText());
    }

    @Test
    void notificationChannelCarriesTheZoneItIsIn() {
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setId(7L);
        channel.setUpdatedAt(STORED);

        JsonNode json = objectMapper.valueToTree(
                new AlarmNotificationChannelApiMapper(objectMapper).toResponse(channel));

        assertEquals(SENT, json.get("updatedAt").asText());
    }

    @Test
    void aTimestampSentBackIsReadAsTheClockItWasSentIn() throws Exception {
        assertEquals(STORED, readUpdatedAt(SENT));
        // The offset is what is reduced away; with Z there is nothing to reduce.
        assertEquals(STORED, readUpdatedAt("2026-09-22T04:50:12+09:00"));
        // No offset at all is taken as the wall clock the column already holds.
        assertEquals(STORED, readUpdatedAt("2026-09-21T19:50:12"));
    }

    @Test
    void aTimestampFieldIsNotSomewhereToHideTheRestOfTheBody() {
        // Asking a non-scalar token for its text answers null without consuming the subtree, and
        // the bean deserializer then reads that subtree's members as properties of the rule.
        assertThrows(MismatchedInputException.class, () -> objectMapper.readValue(
                "{\"applicationName\":\"a\",\"updatedAt\":{\"applicationName\":\"victim\"}}",
                AlarmRuleV2.class));
        assertThrows(MismatchedInputException.class, () -> objectMapper.readValue(
                "{\"applicationName\":\"a\",\"updatedAt\":[1,2]}", AlarmRuleV2.class));
    }

    @Test
    void everyTimestampAnAlarmResponseCarriesIsAnnotated() {
        List<Class<?>> serialized = List.of(
                AlarmState.class, AlarmHistoryV2.class, AlarmRuleV2.class,
                AlarmTemplate.class, AlarmTemplateItem.class,
                AlarmNotificationChannelResponse.class);

        for (Class<?> type : serialized) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() != LocalDateTime.class) {
                    continue;
                }
                JsonSerialize annotation = field.getAnnotation(JsonSerialize.class);
                assertEquals(UtcTimestampSerializer.class,
                        annotation == null ? null : annotation.using(),
                        type.getSimpleName() + "." + field.getName()
                                + " goes out without saying which zone it is in");
            }
        }
    }

    private LocalDateTime readUpdatedAt(String sent) throws Exception {
        return objectMapper.readValue("{\"id\":58,\"updatedAt\":\"" + sent + "\"}",
                AlarmRuleV2.class).getUpdatedAt();
    }
}
