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

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.profiler.name.Base64Utils;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.Base16Utils;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import com.navercorp.pinpoint.common.server.util.Utf8;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Formats an {@link InstrumentationScope} identity for the OPENTELEMETRY_SCOPE annotation:
     * {@code "name@version"}, or bare {@code "name"} when the version is absent. Returns
     * {@code null} when the scope name is empty (SDK did not populate the scope) so callers
     * omit the annotation entirely.
     */
    public static @Nullable String formatScope(@Nullable InstrumentationScope scope) {
        if (scope == null) {
            return null;
        }
        final String name = scope.getName();
        if (name.isEmpty()) {
            return null;
        }
        final String version = scope.getVersion();
        if (version.isEmpty()) {
            return name;
        }
        return name + '@' + version;
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
        // Validates length (exactly 8 bytes — no silent truncation of longer IDs) and rejects all-zero.
        return OtlpIdValidator.validateSpanId(bytes);
    }

    public static long getParentSpanId(ByteString bytes) {
        // An absent parent (root span) is represented as -1; a present parent must be a valid span ID.
        if (ByteStringUtils.isEmpty(bytes)) {
            return -1;
        }
        return OtlpIdValidator.validateSpanId(bytes);
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
        // no listener: plain conversion without truncation (maxBytes is never read on that path)
        return getAttributeToMap(keyValueList, 0, null);
    }

    /**
     * Converts OTel attributes to a JSON-serializable map, recursing into nested arrays/maps. When
     * {@code onTruncated} is non-null, over-long string / byte leaf values are truncated to
     * {@code maxBytes} (bytes on their hex form) in the same pass, invoking the listener once per
     * truncated leaf; a {@code null} listener renders without truncation.
     */
    public static Map<String, Object> getAttributeToMap(List<KeyValue> keyValueList, int maxBytes, @Nullable TruncationListener onTruncated) {
        final int size = keyValueList.size();
        if (size == 0) {
            return Map.of();
        }
        Map<String, Object> map = new HashMap<>(size);
        for (KeyValue kv : keyValueList) {
            map.put(kv.getKey(), getAttributeValueToValue(kv.getValue(), maxBytes, onTruncated));
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
        return getArrayValueToList(arrayValue, 0, null);
    }

    public static List<Object> getArrayValueToList(ArrayValue arrayValue, int maxBytes, @Nullable TruncationListener onTruncated) {
        final int size = arrayValue.getValuesCount();
        if (size == 0) {
            return List.of();
        }

        final List<AnyValue> valuesList = arrayValue.getValuesList();
        List<Object> list = new ArrayList<>(valuesList.size());
        for (AnyValue anyValue : valuesList) {
            list.add(getAttributeValueToValue(anyValue, maxBytes, onTruncated));
        }
        return list;
    }

    public static Object getAttributeValueToValue(AnyValue anyValue) {
        return getAttributeValueToValue(anyValue, 0, null);
    }

    public static Object getAttributeValueToValue(AnyValue anyValue, int maxBytes, @Nullable TruncationListener onTruncated) {
        return switch (anyValue.getValueCase()) {
            case INT_VALUE -> anyValue.getIntValue();
            case DOUBLE_VALUE -> anyValue.getDoubleValue();
            case BOOL_VALUE -> anyValue.getBoolValue();
            case STRING_VALUE -> transformString(anyValue.getStringValue(), maxBytes, onTruncated);
            case BYTES_VALUE -> transformBytes(anyValue.getBytesValue(), maxBytes, onTruncated);
            case ARRAY_VALUE -> getArrayValueToList(anyValue.getArrayValue(), maxBytes, onTruncated);
            case KVLIST_VALUE -> getAttributeToMap(anyValue.getKvlistValue().getValuesList(), maxBytes, onTruncated);
            default -> null;
        };
    }

    /**
     * Streams OTel attributes as a JSON object directly to {@code generator}, recursing into
     * nested arrays/maps — the streaming counterpart of
     * {@link #getAttributeToMap(List, int, TruncationListener)} with the same truncation semantics.
     */
    public static void writeAttributes(JsonGenerator generator, List<KeyValue> keyValueList, int maxBytes, @Nullable TruncationListener onTruncated) throws IOException {
        generator.writeStartObject();
        for (KeyValue kv : keyValueList) {
            generator.writeFieldName(kv.getKey());
            writeAnyValue(generator, kv.getValue(), maxBytes, onTruncated);
        }
        generator.writeEndObject();
    }

    public static void writeAnyValue(JsonGenerator generator, AnyValue anyValue, int maxBytes, @Nullable TruncationListener onTruncated) throws IOException {
        switch (anyValue.getValueCase()) {
            case INT_VALUE -> generator.writeNumber(anyValue.getIntValue());
            case DOUBLE_VALUE -> generator.writeNumber(anyValue.getDoubleValue());
            case BOOL_VALUE -> generator.writeBoolean(anyValue.getBoolValue());
            case STRING_VALUE -> generator.writeString(transformString(anyValue.getStringValue(), maxBytes, onTruncated));
            case BYTES_VALUE -> generator.writeString(transformBytes(anyValue.getBytesValue(), maxBytes, onTruncated));
            case ARRAY_VALUE -> {
                generator.writeStartArray();
                for (AnyValue element : anyValue.getArrayValue().getValuesList()) {
                    writeAnyValue(generator, element, maxBytes, onTruncated);
                }
                generator.writeEndArray();
            }
            case KVLIST_VALUE -> writeAttributes(generator, anyValue.getKvlistValue().getValuesList(), maxBytes, onTruncated);
            default -> generator.writeNull();
        }
    }

    private static String transformString(String value, int maxBytes, @Nullable TruncationListener onTruncated) {
        if (onTruncated == null) {
            return value;
        }
        final String truncated = Utf8.truncate(value, maxBytes);
        if (truncated != null) {
            onTruncated.truncated();
            return truncated;
        }
        return value;
    }

    private static String transformBytes(ByteString bytes, int maxBytes, @Nullable TruncationListener onTruncated) {
        // empty bytes -> "" (Base16 of an empty array), symmetric with the empty STRING case
        if (onTruncated == null) {
            return ByteStringUtils.encodeBase16(bytes);
        }
        if (Base16Utils.encodedLength(bytes.size()) > maxBytes) {
            onTruncated.truncated();
        }
        return ByteStringUtils.encodeBase16(bytes, maxBytes);
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

}
