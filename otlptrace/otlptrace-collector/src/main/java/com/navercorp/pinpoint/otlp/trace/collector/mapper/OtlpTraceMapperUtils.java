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

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.profiler.name.Base64Utils;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

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

    public static Map<String, Object> getAttributeToMap(List<KeyValue> keyValueList) {
        final int size = keyValueList.size();
        if (size == 0) {
            return Map.of();
        }
        Map<String, Object> map = new HashMap<>(size);
        for (KeyValue kv : keyValueList) {
            map.put(kv.getKey(), getAttributeValueToValue(kv.getValue()));
        }
        return map;
    }

    public static Map<String, Object> getAttributeToMap(List<KeyValue> keyValueList, Predicate<String> excludeFilter) {
        final int size = keyValueList.size();
        if (size == 0) {
            return Map.of();
        }
        Map<String, Object> map = new HashMap<>(size);
        for (KeyValue kv : keyValueList) {
            String key = kv.getKey();
            if (excludeFilter.test(key)) {
                continue;
            }
            map.put(key, getAttributeValueToValue(kv.getValue()));
        }
        return map;
    }

    public static List<Object> getArrayValueToList(ArrayValue arrayValue) {
        final int size = arrayValue.getValuesCount();
        if (size == 0) {
            return List.of();
        }

        List<Object> list = new ArrayList<>(size);
        for (AnyValue anyValue : arrayValue.getValuesList()) {
            list.add(getAttributeValueToValue(anyValue));
        }
        return list;
    }

    public static Object getAttributeValueToValue(AnyValue anyValue) {
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

}
