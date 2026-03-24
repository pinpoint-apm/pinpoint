package com.navercorp.pinpoint.web.filter;

import com.fasterxml.jackson.databind.JsonNode;

public final class JsonNodeUtils {

    private JsonNodeUtils() {
    }

    public static String textValue(JsonNode jsonNode, String fieldName) {
        return textValue(jsonNode, fieldName, null);
    }

    public static String textValue(JsonNode jsonNode, String fieldName, String defaultValue) {
        JsonNode node = jsonNode.get(fieldName);
        if (isNull(node)) {
            return defaultValue;
        }
        return node.asText();
    }

    public static Long longValue(JsonNode jsonNode, String fieldName) {
        JsonNode node = jsonNode.get(fieldName);
        if (isNull(node)) {
            return null;
        }
        return node.asLong();
    }

    public static Boolean booleanValue(JsonNode jsonNode, String fieldName) {
        JsonNode node = jsonNode.get(fieldName);
        if (isNull(node)) {
            return null;
        }
        return node.asBoolean();
    }

    private static boolean isNull(JsonNode node) {
        return node == null || node.isNull();
    }
}