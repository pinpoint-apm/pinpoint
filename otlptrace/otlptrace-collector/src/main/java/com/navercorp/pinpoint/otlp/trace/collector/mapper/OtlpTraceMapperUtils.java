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
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.Base16Utils;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class OtlpTraceMapperUtils {
    private static final String KEY_AGENT_ID = "pinpoint.agentId";
    private static final String KEY_AGENT_NAME = "pinpoint.agentName";
    private static final String KEY_APPLICATION_NAME = "pinpoint.applicationName";
    private static final String KEY_SERVICE_INSTANCE_ID = "service.instance.id";
    private static final String KEY_K8S_POD_UID = "k8s.pod.uid";
    private static final String KEY_CONTAINER_ID = "container.id";
    private static final String KEY_HOST_NAME = "host.name";
    private static final String KEY_SERVICE_NAME = "service.name";
    private static final String KEY_PINPOINT_SERVICE_NAME = "pinpoint.serviceName";
    private static final String KEY_SERVICE_NAMESPACE = "service.namespace";

    private static final int CONTAINER_ID_FULL_HEX_LEN = 64;
    private static final int AGENT_ID_HASH_PREFIX_BYTES = 16;

    public static IdAndName getId(Map<String, AttributeValue> attributes) {
        return getId(attributes, false);
    }

    public static IdAndName getId(Map<String, AttributeValue> attributes, boolean allowApplicationNameFallback) {
        final String applicationName = getApplicationName(attributes);
        final String agentNameOverride = getAgentNameOverride(attributes);
        final AgentAuth agentAuth = getAgentAuth(attributes, agentNameOverride, applicationName, allowApplicationNameFallback);
        final String serviceName = getServiceName(attributes);
        return new IdAndName(agentAuth.agentId(), agentAuth.agentName(), applicationName, serviceName);
    }

    private static String resolveAgentName(String agentNameOverride, String defaultName) {
        return agentNameOverride != null ? agentNameOverride : defaultName;
    }

    private static String getAgentNameOverride(Map<String, AttributeValue> attributes) {
        final String agentName = AttributeUtils.getAttributeStringValue(attributes, KEY_AGENT_NAME, null);
        if (agentName == null) {
            return null;
        }
        if (!IdValidateUtils.validateId(agentName, PinpointConstants.AGENT_NAME_MAX_LEN_V4)) {
            throw new IllegalArgumentException("invalid pinpoint.agentName=" + agentName);
        }
        return agentName;
    }

    public static String getApplicationName(Map<String, AttributeValue> attributes) {
        String applicationName = AttributeUtils.getAttributeStringValue(attributes, KEY_APPLICATION_NAME, null);
        if (applicationName == null) {
            applicationName = AttributeUtils.getAttributeStringValue(attributes, KEY_SERVICE_NAME, null);
            if (applicationName == null) {
                throw new IllegalArgumentException("not found applicationName");
            }
        }
        if (!IdValidateUtils.validateId(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3)) {
            throw new IllegalArgumentException("invalid applicationName=" + applicationName);
        }

        return applicationName;
    }

    private record AgentAuth(String agentId, String agentName) {
    }

    private static AgentAuth getAgentAuth(Map<String, AttributeValue> attributes, String agentNameOverride, String applicationName, boolean allowApplicationNameFallback) {
        final String agentId = AttributeUtils.getAttributeStringValue(attributes, KEY_AGENT_ID, null);
        if (agentId != null) {
            if (!IdValidateUtils.validateId(agentId, PinpointConstants.AGENT_ID_MAX_LEN)) {
                throw new IllegalArgumentException("invalid pinpoint.agentId=" + agentId);
            }
            return new AgentAuth(agentId, resolveAgentName(agentNameOverride, agentId));
        }

        final String serviceInstanceId = AttributeUtils.getAttributeStringValue(attributes, KEY_SERVICE_INSTANCE_ID, null);
        if (serviceInstanceId != null) {
            return toAgentAuth(serviceInstanceId, KEY_SERVICE_INSTANCE_ID, agentNameOverride);
        }

        final String podUid = AttributeUtils.getAttributeStringValue(attributes, KEY_K8S_POD_UID, null);
        if (podUid != null) {
            return toAgentAuth(podUid, KEY_K8S_POD_UID, agentNameOverride);
        }

        final String containerId = AttributeUtils.getAttributeStringValue(attributes, KEY_CONTAINER_ID, null);
        if (containerId != null) {
            return toContainerAgentAuth(containerId, agentNameOverride);
        }

        final String hostName = AttributeUtils.getAttributeStringValue(attributes, KEY_HOST_NAME, null);
        if (hostName != null) {
            if (!IdValidateUtils.validateId(hostName, PinpointConstants.AGENT_ID_MAX_LEN)) {
                throw new IllegalArgumentException("invalid host.name=" + hostName);
            }
            return new AgentAuth(hostName, resolveAgentName(agentNameOverride, hostName));
        }

        // applicationName fallback — test/dev environment only.
        // Gated by pinpoint.collector.otlptrace.application-name-fallback.enabled (default: false).
        if (!allowApplicationNameFallback) {
            throw new IllegalArgumentException("no per-instance identifier — set service.instance.id (e.g. via uuidgen), k8s.pod.uid, container.id, or host.name. applicationName='" + applicationName + "'");
        }
        if (!IdValidateUtils.validateId(applicationName, PinpointConstants.AGENT_ID_MAX_LEN)) {
            throw new IllegalArgumentException("invalid agentId(derived from applicationName)=" + applicationName);
        }
        return new AgentAuth(applicationName, resolveAgentName(agentNameOverride, applicationName));
    }

    private static AgentAuth toAgentAuth(String id, String sourceKey, String agentNameOverride) {
        if (id.length() == 36) {
            try {
                final UUID uuid = UUID.fromString(id);
                return new AgentAuth(Base64Utils.encode(uuid), resolveAgentName(agentNameOverride, id));
            } catch (IllegalArgumentException ignore) {
                // not a valid UUID string, fall through to treat as plain agentId
            }
        }
        if (!IdValidateUtils.validateId(id, PinpointConstants.AGENT_ID_MAX_LEN)) {
            throw new IllegalArgumentException("invalid " + sourceKey + "=" + id);
        }
        return new AgentAuth(id, resolveAgentName(agentNameOverride, id));
    }

    private static AgentAuth toContainerAgentAuth(String containerId, String agentNameOverride) {
        // Docker/containerd full ID: 64 lowercase hex chars (SHA256).
        // Truncate to first 16 bytes → 22-char URL-safe Base64 (same format as UUID case).
        if (containerId.length() == CONTAINER_ID_FULL_HEX_LEN) {
            try {
                final byte[] bytes = Base16Utils.decodeToBytes(containerId);
                final byte[] prefix = Arrays.copyOf(bytes, AGENT_ID_HASH_PREFIX_BYTES);
                return new AgentAuth(Base64Utils.encode(prefix), resolveAgentName(agentNameOverride, containerId));
            } catch (IllegalArgumentException ignore) {
                // not valid hex, fall through to treat as plain agentId
            }
        }
        if (!IdValidateUtils.validateId(containerId, PinpointConstants.AGENT_ID_MAX_LEN)) {
            throw new IllegalArgumentException("invalid " + KEY_CONTAINER_ID + "=" + containerId);
        }
        return new AgentAuth(containerId, resolveAgentName(agentNameOverride, containerId));
    }

    public static String getServiceName(Map<String, AttributeValue> attributes) {
        String serviceName = AttributeUtils.getAttributeStringValue(attributes, KEY_PINPOINT_SERVICE_NAME, null);
        if (serviceName == null) {
            serviceName = AttributeUtils.getAttributeStringValue(attributes, KEY_SERVICE_NAMESPACE, null);
        }
        if (serviceName == null) {
            return ServiceUid.DEFAULT_SERVICE_UID_NAME;
        }
        if (!IdValidateUtils.validateId(serviceName, PinpointConstants.SERVICE_NAME_MAX_LEN)) {
            throw new IllegalArgumentException("invalid serviceName=" + serviceName);
        }
        return serviceName;
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

    public static Map<String, AttributeValue> getAttributeValueMap(List<KeyValue> keyValueList) {
        final int size = keyValueList.size();
        if (size == 0) {
            return Map.of();
        }
        Map<String, AttributeValue> map = new HashMap<>(size);
        for (KeyValue kv : keyValueList) {
            AttributeValue av = toAttributeValue(kv.getValue());
            if (av != null) {
                map.put(kv.getKey(), av);
            }
        }
        return map;
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
        switch (anyValue.getValueCase()) {
            case INT_VALUE:
                return anyValue.getIntValue();
            case DOUBLE_VALUE:
                return anyValue.getDoubleValue();
            case BOOL_VALUE:
                return anyValue.getBoolValue();
            case STRING_VALUE:
                return anyValue.getStringValue();
            case ARRAY_VALUE:
                return getArrayValueToList(anyValue.getArrayValue());
            case BYTES_VALUE:
                final ByteString byteString = anyValue.getBytesValue();
                if (!byteString.isEmpty()) {
                    return Base16Utils.encodeToString(byteString.toByteArray());
                }
                return null;
            case KVLIST_VALUE:
                return getAttributeToMap(anyValue.getKvlistValue().getValuesList());
            default:
                return null;
        }
    }

    public static AttributeValue toAttributeValue(AnyValue anyValue) {
        return switch (anyValue.getValueCase()) {
            case STRING_VALUE -> AttributeValue.of(anyValue.getStringValue());
            case BOOL_VALUE -> AttributeValue.of(anyValue.getBoolValue());
            case INT_VALUE -> AttributeValue.of(anyValue.getIntValue());
            case DOUBLE_VALUE -> AttributeValue.of(anyValue.getDoubleValue());
            case BYTES_VALUE -> AttributeValue.of(anyValue.getBytesValue().toByteArray());
            case ARRAY_VALUE -> {
                List<AttributeValue> list = new ArrayList<>();
                for (AnyValue item : anyValue.getArrayValue().getValuesList()) {
                    AttributeValue av = toAttributeValue(item);
                    if (av != null) {
                        list.add(av);
                    }
                }
                yield AttributeValue.of(list);
            }
            case KVLIST_VALUE -> {
                List<KeyValue> kvList = anyValue.getKvlistValue().getValuesList();
                List<AttributeKeyValue> result = new ArrayList<>(kvList.size());
                for (KeyValue kv : kvList) {
                    AttributeValue av = toAttributeValue(kv.getValue());
                    if (av != null) {
                        result.add(AttributeKeyValue.of(kv.getKey(), av));
                    }
                }
                yield AttributeValue.ofAttributeKeyValueList(result.toArray(new AttributeKeyValue[0]));
            }
            default -> null;
        };
    }

    public static List<AttributeBo> toAttributeBoList(Map<String, AttributeValue> attributes, Predicate<String> excludeFilter) {
        if (attributes.isEmpty()) {
            return List.of();
        }
        List<AttributeBo> result = new ArrayList<>();
        for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
            if (excludeFilter.test(entry.getKey())) {
                continue;
            }
            result.add(new AttributeBo(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * Truncates over-long string / byte attribute values in place (UTF-8 byte length), recursing
     * into ARRAY / KVLIST. Numeric and boolean values are left untouched, matching the OTel spec
     * (only string and byte values are length-limited). Returns the number of leaf values truncated
     * so the caller can emit a single per-span {@code OPENTELEMETRY_TRUNCATED} summary.
     */
    public static int truncateAttributeValues(List<AttributeBo> attributeBoList, int maxValueBytes) {
        int truncated = 0;
        final ListIterator<AttributeBo> iterator = attributeBoList.listIterator();
        while (iterator.hasNext()) {
            final AttributeBo bo = iterator.next();
            final int[] counter = {0};
            final AttributeValue value = truncateValue(bo.getValue(), maxValueBytes, counter);
            if (counter[0] > 0) {
                iterator.set(new AttributeBo(bo.getKey(), value));
                truncated += counter[0];
            }
        }
        return truncated;
    }

    /**
     * Truncates a string to at most {@code maxBytes} UTF-8 bytes without splitting a multi-byte
     * character. Returns {@code null} when no truncation is needed (value already within the limit).
     */
    public static String truncateUtf8(String value, int maxBytes) {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return null;
        }
        int end = maxBytes;
        // back off so a multi-byte UTF-8 sequence is not split (0b10xxxxxx == continuation byte)
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) {
            end--;
        }
        return new String(bytes, 0, end, StandardCharsets.UTF_8);
    }

    /**
     * Truncates over-long string values (UTF-8 byte length) in an {@code Object} map produced by
     * {@link #getAttributeToMap}, recursing into nested maps/lists. Used for event/link attributes
     * that are serialized to JSON, keeping the JSON valid. Returns the number of values truncated.
     */
    @SuppressWarnings("unchecked")
    public static int truncateStringValues(Map<String, Object> map, int maxBytes) {
        int count = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof String s) {
                final String truncated = truncateUtf8(s, maxBytes);
                if (truncated != null) {
                    entry.setValue(truncated);
                    count++;
                }
            } else if (value instanceof Map) {
                count += truncateStringValues((Map<String, Object>) value, maxBytes);
            } else if (value instanceof List) {
                count += truncateStringValues((List<Object>) value, maxBytes);
            }
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    public static int truncateStringValues(List<Object> list, int maxBytes) {
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            final Object value = list.get(i);
            if (value instanceof String s) {
                final String truncated = truncateUtf8(s, maxBytes);
                if (truncated != null) {
                    list.set(i, truncated);
                    count++;
                }
            } else if (value instanceof Map) {
                count += truncateStringValues((Map<String, Object>) value, maxBytes);
            } else if (value instanceof List) {
                count += truncateStringValues((List<Object>) value, maxBytes);
            }
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private static AttributeValue truncateValue(AttributeValue value, int maxBytes, int[] counter) {
        switch (value.getType()) {
            case STRING: {
                final String truncated = truncateUtf8((String) value.getValue(), maxBytes);
                if (truncated == null) {
                    return value;
                }
                counter[0]++;
                return AttributeValue.of(truncated);
            }
            case BYTES: {
                final byte[] bytes = (byte[]) value.getValue();
                if (bytes.length <= maxBytes) {
                    return value;
                }
                counter[0]++;
                return AttributeValue.of(Arrays.copyOf(bytes, maxBytes));
            }
            case ARRAY: {
                final List<AttributeValue> array = (List<AttributeValue>) value.getValue();
                boolean changed = false;
                final List<AttributeValue> result = new ArrayList<>(array.size());
                for (AttributeValue item : array) {
                    final AttributeValue newItem = truncateValue(item, maxBytes, counter);
                    result.add(newItem);
                    changed |= (newItem != item);
                }
                return changed ? AttributeValue.of(result) : value;
            }
            case KEY_VALUE_LIST: {
                final List<AttributeKeyValue> kvList = (List<AttributeKeyValue>) value.getValue();
                boolean changed = false;
                final AttributeKeyValue[] result = new AttributeKeyValue[kvList.size()];
                for (int i = 0; i < kvList.size(); i++) {
                    final AttributeKeyValue entry = kvList.get(i);
                    final AttributeValue newValue = truncateValue(entry.getValue(), maxBytes, counter);
                    final boolean entryChanged = newValue != entry.getValue();
                    result[i] = entryChanged ? AttributeKeyValue.of(entry.getKey(), newValue) : entry;
                    changed |= entryChanged;
                }
                return changed ? AttributeValue.ofAttributeKeyValueList(result) : value;
            }
            default:
                // BOOLEAN / LONG / DOUBLE — never truncated (per OTel spec)
                return value;
        }
    }
}
