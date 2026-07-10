package com.navercorp.pinpoint.web.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.jspecify.annotations.Nullable;

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

    public static boolean isNull(@Nullable JsonNode node) {
        return node == null || node.isNull() || node.isMissingNode();
    }
}