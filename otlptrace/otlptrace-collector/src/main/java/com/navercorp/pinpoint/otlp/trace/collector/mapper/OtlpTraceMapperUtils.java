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
import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.profiler.name.Base64Utils;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.server.util.Base16Utils;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OtlpTraceMapperUtils {
    private static final String KEY_AGENT_ID = "pinpoint.agentId";
    private static final String KEY_APPLICATION_NAME = "pinpoint.applicationName";
    private static final String KEY_SERVICE_INSTANCE_ID = "service.instance.id";
    private static final String KEY_SERVICE_NAME = "service.name";

    public static IdAndName getId(List<KeyValue> attributesList) {
        final String agentId = attributesList.stream().filter(kv -> kv.getKey().equals(KEY_AGENT_ID)).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (agentId == null) {
            final String serviceInstanceId = attributesList.stream().filter(kv -> kv.getKey().equals(KEY_SERVICE_INSTANCE_ID)).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
            if (serviceInstanceId == null) {
                throw new IllegalArgumentException("not found agentId");
            }
            // check UUID
            if (serviceInstanceId.length() == 36) {
                final UUID uuid = UUID.fromString(serviceInstanceId);
                final String encoded = Base64Utils.encode(uuid.toString());
                return new IdAndName(encoded, serviceInstanceId, getApplicationName(attributesList));
            }
            // agentId
            if (!IdValidateUtils.validateId(serviceInstanceId, PinpointConstants.AGENT_ID_MAX_LEN)) {
                throw new IllegalArgumentException("invalid agentId=" + serviceInstanceId);
            }
            return new IdAndName(serviceInstanceId, null, getApplicationName(attributesList));
        }

        if (!IdValidateUtils.validateId(agentId, PinpointConstants.AGENT_ID_MAX_LEN)) {
            throw new IllegalArgumentException("invalid agentId=" + agentId);
        }

        return new IdAndName(agentId, null, getApplicationName(attributesList));
    }

    public static String getApplicationName(List<KeyValue> attributesList) {
        String applicationName = attributesList.stream().filter(kv -> kv.getKey().equals(KEY_APPLICATION_NAME)).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (applicationName == null) {
            applicationName = attributesList.stream().filter(kv -> kv.getKey().equals(KEY_SERVICE_NAME)).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
            if (applicationName == null) {
                throw new IllegalArgumentException("not found applicationName");
            }
        }
        if (!IdValidateUtils.validateId(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3)) {
            throw new IllegalArgumentException("invalid applicationName=" + applicationName);
        }

        return applicationName;
    }

    public static long getSpanId(ByteString bytes) {
        if (ByteStringUtils.isEmpty(bytes)) {
            throw new IllegalArgumentException("not found spanId");
        }

        return ByteStringUtils.parseLong(bytes);
    }

    public static long getParentSpanId(ByteString bytes) {
        if (ByteStringUtils.isEmpty(bytes)) {
            return -1;
        }
        return ByteStringUtils.parseLong(bytes);
    }

    public static void addAttributesToAnnotation(ObjectMapper objectMapper, List<KeyValue> keyValueList, AnnotationWriter annotationWriter) {
        try {
            final Map<String, Object> map = getAttributeToMap(keyValueList);
            if (!map.isEmpty()) {
                map.entrySet().removeIf(entry -> OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY_MAP.containsKey(entry.getKey()));
                final String value = objectMapper.writeValueAsString(map);
                annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), value));
            }
        } catch (JsonProcessingException e) {
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), "json processing error"));
        }
    }


    static Map<String, Object> getAttributeToMap(List<KeyValue> keyValueList) {
        Map<String, Object> map = new HashMap<>();
        for (KeyValue kv : keyValueList) {
            map.put(kv.getKey(), getAttributeValueToValue(kv.getValue()));
        }
        return map;
    }

    static List<Object> getArrayValueToList(ArrayValue arrayValue) {
        List<Object> list = new ArrayList<>();
        for (AnyValue anyValue : arrayValue.getValuesList()) {
            list.add(getAttributeValueToValue(anyValue));
        }
        return list;
    }

    static Object getAttributeValueToValue(AnyValue anyValue) {
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
            // unknown field
            return null;
        }
    }

    public static void addEventToAnnotation(ObjectMapper objectMapper, Span.Event event, AnnotationWriter annotationWriter) {
        if (event.getName() == null) {
            return;
        }

        try {
            Map<String, Object> map = new HashMap<>();
            if (event.getAttributesCount() > 0) {
                map.put(event.getName(), getAttributeToMap(event.getAttributesList()));
            } else {
                map.put(event.getName(), "");
            }
            final String value = objectMapper.writeValueAsString(map);
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_EVENT.getCode(), value));
        } catch (JsonProcessingException e) {
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_EVENT.getCode(), "json processing error"));
        }
    }

    public static void addLinkToAnnotation(ObjectMapper objectMapper, Span.Link link, AnnotationWriter annotationWriter) {
        try {
            Map<String, Object> map = new HashMap<>();
            if (!link.getTraceId().isEmpty()) {
                map.put("traceId", Base16Utils.encodeToString(link.getTraceId().toByteArray()));
            }
            if (!link.getSpanId().isEmpty()) {
                map.put("spanId", Base16Utils.encodeToString(link.getSpanId().toByteArray()));
            }
            if (link.getAttributesCount() > 0) {
                map.put("attributes", getAttributeToMap(link.getAttributesList()));
            }
            final String value = objectMapper.writeValueAsString(map);
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), value));
        } catch (JsonProcessingException e) {
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), "json processing error"));
        }
    }
}
