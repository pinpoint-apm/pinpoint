package com.navercorp.pinpoint.common.server.util.json;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

public final class TypeRef {
    private TypeRef() {
    }

    private static final TypeReference<Map<String, Object>> MAP_REF = new TypeReference<>() {
    };

    private static final TypeReference<List<Map<String, Object>>> LIST_MAP_REF = new TypeReference<>() {
    };


    public static TypeReference<Map<String, Object>> map() {
        return MAP_REF;
    }

    public static TypeReference<List<Map<String, Object>>> listMap() {
        return LIST_MAP_REF;
    }
}
