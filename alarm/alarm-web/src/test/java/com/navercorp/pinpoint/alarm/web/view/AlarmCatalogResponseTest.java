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
package com.navercorp.pinpoint.alarm.web.view;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AlarmCatalogResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void preservesCatalogApiResponseShape() {
        AlarmCatalogResponse.DataSource response =
                AlarmCatalogResponse.DataSource.from(TestAlarmDataSource.AGENT_STAT);

        JsonNode json = objectMapper.valueToTree(response);

        assertEquals("AGENT_STAT", json.get("value").asString());
        assertEquals("Agent Stat", json.get("label").asString());
        assertEquals("agent", json.get("filterKeys").get(0).asString());
        assertEquals("sample_count", json.get("metrics").get(0).get("value").asString());
        assertFalse(json.get("metrics").get(0).has("trigger"));
        assertEquals("NEW_GROUP", json.get("metrics").get(1).get("trigger").asString());
        assertFalse(json.get("metrics").get(1).has("allowedAggregations"));
    }
}
