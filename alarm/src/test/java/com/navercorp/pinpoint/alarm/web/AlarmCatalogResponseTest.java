package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AlarmCatalogResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void preservesCatalogApiResponseShape() {
        AlarmCatalogResponse.DataSource response =
                AlarmCatalogResponse.DataSource.from(TestAlarmDataSource.PRIMARY);

        JsonNode json = objectMapper.valueToTree(response);

        assertEquals("PRIMARY", json.get("value").asText());
        assertEquals("Primary", json.get("label").asText());
        assertEquals("environment", json.get("filterKeys").get(0).asText());
        assertEquals("event_count", json.get("metrics").get(0).get("value").asText());
        assertFalse(json.get("metrics").get(0).has("trigger"));
        assertEquals("NEW_GROUP", json.get("metrics").get(1).get("trigger").asText());
        assertFalse(json.get("metrics").get(1).has("allowedAggregations"));
    }
}
