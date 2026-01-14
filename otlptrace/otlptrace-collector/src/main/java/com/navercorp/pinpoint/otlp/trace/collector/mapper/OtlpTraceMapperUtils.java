/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtlpTraceMapperUtils {
    public static String getAgentId(List<KeyValue> attributesList) {
        final String agentId = attributesList.stream().filter(kv -> kv.getKey().equals("pinpoint.agentId")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (agentId == null) {
            throw new IllegalStateException("not found agentId");
        }

        return agentId;
    }

    public static String getApplicationName(List<KeyValue> attributesList) {
        final String applicationName = attributesList.stream().filter(kv -> kv.getKey().equals("pinpoint.applicationName")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (applicationName == null) {
            throw new IllegalStateException("not found applicationName");
        }

        return applicationName;
    }

    public static long getSpanId(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            throw new IllegalArgumentException("not found spanId");
        }

        return ByteArrayUtils.bytesToLong(bytes, 0);
    }

    public static long getParentSpanId(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return -1;
        }

        return ByteArrayUtils.bytesToLong(bytes, 0);
    }

    public static void addAttributesToAnnotation(ObjectMapper objectMapper, List<KeyValue> keyValueList, List<AnnotationBo> annotationBoList) {
        try {
            final Map<String, Object> map = getAttributeToMap(keyValueList);
            if (!map.isEmpty()) {
                map.entrySet().removeIf(entry -> OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY_MAP.containsKey(entry.getKey()));
                final String value = objectMapper.writeValueAsString(map);
                annotationBoList.add(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), value));
            }
        } catch (JsonProcessingException e) {
            annotationBoList.add(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), "json processing error"));
        }
    }


    static Map<String, Object> getAttributeToMap(List<KeyValue> keyValueList) {
        Map<String, Object> map = new HashMap<>();
        for (KeyValue kv : keyValueList) {
            map.put(kv.getKey(), getAttriubteValueToValue(kv.getValue()));
        }
        return map;
    }

    static List<Object> getArrayValueToList(ArrayValue arrayValue) {
        List<Object> list = new ArrayList<>(arrayValue.getValuesList());
        for (AnyValue anyValue : arrayValue.getValuesList()) {
            list.add(getAttriubteValueToValue(anyValue));
        }
        return list;
    }

    static Object getAttriubteValueToValue(AnyValue anyValue) {
        if (anyValue.hasIntValue()) {
            return anyValue.getIntValue();
        } else if (anyValue.hasDoubleValue()) {
            return anyValue.getDoubleValue();
        } else if (anyValue.hasBoolValue()) {
            return anyValue.getBoolValue();
        } else if (anyValue.hasStringValue()) {
            return anyValue.getStringValue();
        } else if (anyValue.hasArrayValue()) {
            return getArrayValueToList(anyValue.getArrayValue());
        } else if (anyValue.hasBytesValue()) {
            return anyValue.getBytesValue();
        } else if (anyValue.hasKvlistValue()) {
            return getAttributeToMap(anyValue.getKvlistValue().getValuesList());
        } else {
            return anyValue;
        }
    }
}
