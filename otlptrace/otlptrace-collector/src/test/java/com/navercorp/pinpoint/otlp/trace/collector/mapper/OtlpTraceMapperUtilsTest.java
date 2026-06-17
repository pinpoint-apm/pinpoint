package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueType;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.arrayVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.boolVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.bytesVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.doubleVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.intVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kvlistVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpTraceMapperUtilsTest {

    // =======================================================================
    // getAttributeToMapExcluding(List<KeyValue>, Predicate)
    // =======================================================================

    @Test
    void emptyList_returnsEmptyMap() {
        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(List.of(), key -> false);

        assertThat(result).isEmpty();
    }

    @Test
    void noExclusion_returnsAllEntries() {
        List<KeyValue> attrs = List.of(
                kv("a", strVal("1")),
                kv("b", strVal("2"))
        );

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(attrs, key -> false);

        assertThat(result).containsOnlyKeys("a", "b");
    }

    @Test
    void allKeysExcluded_returnsEmptyMap() {
        List<KeyValue> attrs = List.of(
                kv("x", strVal("v1")),
                kv("y", strVal("v2"))
        );

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(attrs, key -> true);

        assertThat(result).isEmpty();
    }

    @Test
    void partialExclusion_excludedKeysAbsent() {
        Set<String> excluded = Set.of("url.path", "db.statement");
        List<KeyValue> attrs = List.of(
                kv("http.method", strVal("GET")),
                kv("url.path", strVal("/api")),
                kv("db.statement", strVal("SELECT 1"))
        );

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(attrs, excluded::contains);

        assertThat(result).containsOnlyKeys("http.method");
        assertThat(result).containsEntry("http.method", "GET");
    }

    @Test
    void filteredAttributeKeySet_removesKnownKeys() {
        List<KeyValue> attrs = List.of(
                kv("http.method", strVal("POST")),
                kv(OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH, strVal("/save")),
                kv(OtlpTraceConstants.ATTRIBUTE_KEY_DB_STATEMENT, strVal("INSERT INTO t")),
                kv(OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE, strVal("201"))
        );

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(
                attrs, OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY);

        assertThat(result).containsOnlyKeys("http.method");
    }

    @Test
    void valueTypes_preservedAfterFilter() {
        List<KeyValue> attrs = List.of(
                kv("count", intVal(7L)),
                kv("flag", boolVal(true)),
                kv("ratio", doubleVal(3.14)),
                kv("excluded", strVal("drop me"))
        );

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(
                attrs, key -> key.equals("excluded"));

        assertThat(result).containsEntry("count", 7L);
        assertThat(result).containsEntry("flag", true);
        assertThat(result).containsEntry("ratio", 3.14);
        assertThat(result).doesNotContainKey("excluded");
    }

    @Test
    void arrayValue_preservedAfterFilter() {
        ArrayValue arrayValue = ArrayValue.newBuilder()
                .addValues(strVal("x"))
                .addValues(strVal("y"))
                .build();
        List<KeyValue> attrs = List.of(
                kv("tags", AnyValue.newBuilder().setArrayValue(arrayValue).build()),
                kv("excluded", strVal("drop me"))
        );

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(
                attrs, key -> key.equals("excluded"));

        assertThat(result).containsOnlyKeys("tags");
        assertThat(result.get("tags")).isInstanceOf(List.class)
                .asList().containsExactly("x", "y");
    }

    // =======================================================================
    // getAttributeToMap(List<KeyValue>) — no filter
    // =======================================================================

    @Test
    void getAttributeToMap_noFilter_emptyList() {
        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void getAttributeToMap_noFilter_allEntriesPresent() {
        List<KeyValue> attrs = List.of(
                kv("key1", strVal("val1")),
                kv("key2", intVal(100L))
        );

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(attrs);

        assertThat(result).containsEntry("key1", "val1");
        assertThat(result).containsEntry("key2", 100L);
    }

    @Test
    void getAttributeToMap_nonEmptyBytes_hexEncoded() {
        List<KeyValue> attrs = List.of(kv("payload", bytesVal(new byte[]{0x0A, (byte) 0xFF})));

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(attrs);

        // Base16Utils uses lower-case hex (new Base16(true)).
        assertThat(result).containsEntry("payload", "0aff");
    }

    @Test
    void getAttributeToMap_emptyBytes_encodedAsEmptyString() {
        // Base16 of an empty byte array is "" — the key is kept with an empty-string value
        // (symmetric with the empty STRING case), not dropped or mapped to null.
        List<KeyValue> attrs = List.of(kv("payload", bytesVal(new byte[0])));

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(attrs);

        assertThat(result).containsEntry("payload", "");
    }

    @Test
    void getArrayValueToList_emptyBytesElement_encodedAsEmptyString() {
        ArrayValue arrayValue = ArrayValue.newBuilder()
                .addValues(bytesVal(new byte[0]))
                .build();

        List<Object> result = OtlpTraceMapperUtils.getArrayValueToList(arrayValue);

        assertThat(result).containsExactly("");
    }

    // =======================================================================
    // getId(Map<String, AttributeValue>)
    // =======================================================================

    @Test
    void getId_withPinpointAgentId() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("test-agent"),
                "pinpoint.applicationName", AttributeValue.of("test-app")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("test-agent");
        assertThat(result.applicationName()).isEqualTo("test-app");
        assertThat(result.agentName()).isEqualTo("test-agent");
    }

    @Test
    void getId_withServiceInstanceId_plainString() {
        Map<String, AttributeValue> attrs = Map.of(
                "service.instance.id", AttributeValue.of("my-instance"),
                "service.name", AttributeValue.of("my-service")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("my-instance");
        assertThat(result.applicationName()).isEqualTo("my-service");
    }

    @Test
    void getId_withServiceInstanceId_uuid() {
        Map<String, AttributeValue> attrs = Map.of(
                "service.instance.id", AttributeValue.of("550e8400-e29b-41d4-a716-446655440000"),
                "service.name", AttributeValue.of("uuid-service")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        // UUID should be Base64-encoded
        assertThat(result.agentId()).isNotEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(result.agentName()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(result.applicationName()).isEqualTo("uuid-service");
    }

    @Test
    void getId_withK8sPodUid_uuid() {
        Map<String, AttributeValue> attrs = Map.of(
                "k8s.pod.uid", AttributeValue.of("550e8400-e29b-41d4-a716-446655440000"),
                "service.name", AttributeValue.of("pod-service")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isNotEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(result.agentName()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(result.applicationName()).isEqualTo("pod-service");
    }

    @Test
    void getId_withK8sPodUid_afterServiceInstanceId_isIgnored() {
        Map<String, AttributeValue> attrs = Map.of(
                "service.instance.id", AttributeValue.of("instance-1"),
                "k8s.pod.uid", AttributeValue.of("550e8400-e29b-41d4-a716-446655440000"),
                "service.name", AttributeValue.of("svc")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("instance-1");
    }

    @Test
    void getId_invalidK8sPodUid_throws() {
        Map<String, AttributeValue> attrs = Map.of(
                "k8s.pod.uid", AttributeValue.of("bad uid!!"),
                "service.name", AttributeValue.of("svc")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid k8s.pod.uid");
    }

    @Test
    void getId_withContainerId_fullHex() {
        String fullId = "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789";
        Map<String, AttributeValue> attrs = Map.of(
                "container.id", AttributeValue.of(fullId),
                "service.name", AttributeValue.of("container-service")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        // agentId: 22-char URL-safe Base64 (first 16 bytes encoded)
        assertThat(result.agentId()).hasSize(22);
        assertThat(result.agentId()).matches("[A-Za-z0-9_\\-]+");
        // agentName preserves full 64-hex original
        assertThat(result.agentName()).isEqualTo(fullId);
        assertThat(result.applicationName()).isEqualTo("container-service");
    }

    @Test
    void getId_withContainerId_shortHex() {
        // 12 hex (Docker short ID) already fits AGENT_ID_MAX_LEN=24
        Map<String, AttributeValue> attrs = Map.of(
                "container.id", AttributeValue.of("abcdef012345"),
                "service.name", AttributeValue.of("svc")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("abcdef012345");
        assertThat(result.agentName()).isEqualTo("abcdef012345");
    }

    @Test
    void getId_withContainerId_afterK8sPodUid_isIgnored() {
        Map<String, AttributeValue> attrs = Map.of(
                "k8s.pod.uid", AttributeValue.of("550e8400-e29b-41d4-a716-446655440000"),
                "container.id", AttributeValue.of("abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789"),
                "service.name", AttributeValue.of("svc")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        // pod uid takes priority
        assertThat(result.agentName()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void getId_invalidContainerId_throws() {
        Map<String, AttributeValue> attrs = Map.of(
                "container.id", AttributeValue.of("bad id!!"),
                "service.name", AttributeValue.of("svc")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid container.id");
    }

    @Test
    void getId_withHostName() {
        Map<String, AttributeValue> attrs = Map.of(
                "host.name", AttributeValue.of("my-host"),
                "service.name", AttributeValue.of("host-service")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("my-host");
        assertThat(result.applicationName()).isEqualTo("host-service");
    }

    @Test
    void getId_fallbackToApplicationName() {
        Map<String, AttributeValue> attrs = Map.of(
                "service.name", AttributeValue.of("fallback-app")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs, true);

        assertThat(result.agentId()).isEqualTo("fallback-app");
        assertThat(result.applicationName()).isEqualTo("fallback-app");
    }

    @Test
    void getId_applicationNameFallback_disabledByDefault_throws() {
        // Default getId(attrs) keeps the fallback disabled — same behavior as production.
        Map<String, AttributeValue> attrs = Map.of(
                "service.name", AttributeValue.of("fallback-app")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no per-instance identifier");
    }

    @Test
    void getId_noApplicationName_throws() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("agent1")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found applicationName");
    }

    @Test
    void getId_invalidAgentId_throws() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("invalid agent id with spaces!!!"),
                "pinpoint.applicationName", AttributeValue.of("test-app")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid pinpoint.agentId");
    }

    @Test
    void getId_pinpointAgentName_overridesDerivedAgentName() {
        // pinpoint.agentName takes priority over the agentName derived
        // alongside the agentId (here container.id full-hex → 64-hex original).
        String fullId = "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789";
        Map<String, AttributeValue> attrs = Map.of(
                "container.id", AttributeValue.of(fullId),
                "pinpoint.agentName", AttributeValue.of("human-readable-name"),
                "service.name", AttributeValue.of("svc")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).hasSize(22);
        assertThat(result.agentName()).isEqualTo("human-readable-name");
    }

    @Test
    void getId_pinpointAgentName_withPinpointAgentId_keepsBothIndependent() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("short-id"),
                "pinpoint.agentName", AttributeValue.of("human-readable-name"),
                "pinpoint.applicationName", AttributeValue.of("app")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("short-id");
        assertThat(result.agentName()).isEqualTo("human-readable-name");
    }

    @Test
    void getId_pinpointAgentName_absent_preservesDerivedAgentName() {
        // Without pinpoint.agentName the original UUID is preserved as agentName
        // (existing behavior — fallback must not clobber it).
        Map<String, AttributeValue> attrs = Map.of(
                "service.instance.id", AttributeValue.of("550e8400-e29b-41d4-a716-446655440000"),
                "service.name", AttributeValue.of("svc")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentName()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void getId_pinpointAgentName_doesNotOverrideHostNameDerivedAgentId() {
        // host.name is processed before the agentName-derived fallback,
        // so agentId comes from host.name even when pinpoint.agentName is present.
        Map<String, AttributeValue> attrs = Map.of(
                "host.name", AttributeValue.of("my-host"),
                "pinpoint.agentName", AttributeValue.of("order-api-prod-seoul-pod-7f8d3a"),
                "pinpoint.applicationName", AttributeValue.of("app")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("my-host");
        assertThat(result.agentName()).isEqualTo("order-api-prod-seoul-pod-7f8d3a");
    }

    @Test
    void getId_pinpointAgentName_only_fallsThroughToApplicationName() {
        // agentName alone (no per-instance identifier) → agentId derived from applicationName
        // when fallback is allowed (test/dev). agentName is preserved as the display name.
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentName", AttributeValue.of("human-readable-name"),
                "pinpoint.applicationName", AttributeValue.of("app")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs, true);

        assertThat(result.agentId()).isEqualTo("app");
        assertThat(result.agentName()).isEqualTo("human-readable-name");
    }

    @Test
    void getId_invalidPinpointAgentName_throws() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("agent1"),
                "pinpoint.agentName", AttributeValue.of("name with spaces!!!"),
                "pinpoint.applicationName", AttributeValue.of("app")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid pinpoint.agentName");
    }

    @Test
    void getId_pinpointApplicationName_hasPriority() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("agent1"),
                "pinpoint.applicationName", AttributeValue.of("pinpoint-app"),
                "service.name", AttributeValue.of("otel-service")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.applicationName()).isEqualTo("pinpoint-app");
    }

    @Test
    void getId_serviceName_absent_returnsDefault() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("agent1"),
                "pinpoint.applicationName", AttributeValue.of("app")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.serviceName()).isEqualTo(ServiceUid.DEFAULT_SERVICE_UID_NAME);
    }

    @Test
    void getId_serviceName_fromPinpointServiceName() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("agent1"),
                "pinpoint.applicationName", AttributeValue.of("app"),
                "pinpoint.serviceName", AttributeValue.of("my-service")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.serviceName()).isEqualTo("my-service");
    }

    @Test
    void getId_serviceName_fromServiceNamespace() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("agent1"),
                "pinpoint.applicationName", AttributeValue.of("app"),
                "service.namespace", AttributeValue.of("otel-ns")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.serviceName()).isEqualTo("otel-ns");
    }

    @Test
    void getId_serviceName_pinpointHasPriorityOverNamespace() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("agent1"),
                "pinpoint.applicationName", AttributeValue.of("app"),
                "pinpoint.serviceName", AttributeValue.of("pinpoint-service"),
                "service.namespace", AttributeValue.of("otel-ns")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.serviceName()).isEqualTo("pinpoint-service");
    }

    @Test
    void getId_invalidHostName_throws() {
        Map<String, AttributeValue> attrs = Map.of(
                "host.name", AttributeValue.of("host with spaces!!!"),
                "service.name", AttributeValue.of("svc")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid host.name");
    }

    @Test
    void getId_invalidServiceInstanceId_throws() {
        Map<String, AttributeValue> attrs = Map.of(
                "service.instance.id", AttributeValue.of("bad id!!"),
                "service.name", AttributeValue.of("svc")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid service.instance.id");
    }

    @Test
    void getId_serviceInstanceId_uuidLengthButInvalid_throws() {
        // 36 chars, fails UUID.fromString, falls through to plain agentId validation
        // (which fails because length > AGENT_ID_MAX_LEN=24)
        Map<String, AttributeValue> attrs = Map.of(
                "service.instance.id", AttributeValue.of("gggggggg-gggg-gggg-gggg-gggggggggggg"),
                "service.name", AttributeValue.of("svc")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid service.instance.id");
    }

    @Test
    void getId_applicationNameFallback_exceedsAgentIdLength_throws() {
        // applicationName is valid for V3 (<=254) but exceeds AGENT_ID_MAX_LEN (24)
        // — when fallback is allowed and no other identifier is set, validation rejects it.
        Map<String, AttributeValue> attrs = Map.of(
                "service.name", AttributeValue.of("application-name-that-is-longer-than-24-chars")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid agentId(derived from applicationName)");
    }

    // =======================================================================
    // getApplicationName(Map<String, AttributeValue>)
    // =======================================================================

    @Test
    void getApplicationName_pinpointApplicationName() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.applicationName", AttributeValue.of("my-app")
        );

        String result = OtlpTraceMapperUtils.getApplicationName(attrs);

        assertThat(result).isEqualTo("my-app");
    }

    @Test
    void getApplicationName_fallbackToServiceName() {
        Map<String, AttributeValue> attrs = Map.of(
                "service.name", AttributeValue.of("otel-service")
        );

        String result = OtlpTraceMapperUtils.getApplicationName(attrs);

        assertThat(result).isEqualTo("otel-service");
    }

    @Test
    void getApplicationName_notFound_throws() {
        Map<String, AttributeValue> attrs = Map.of();

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getApplicationName(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found applicationName");
    }

    @Test
    void getApplicationName_invalidName_throws() {
        Map<String, AttributeValue> attrs = Map.of(
                "service.name", AttributeValue.of("invalid name with spaces!!!")
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getApplicationName(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid applicationName");
    }

    // =======================================================================
    // getSpanId(ByteString)
    // =======================================================================

    private static ByteString longToByteString(long value) {
        byte[] bytes = ByteBuffer.allocate(8).putLong(value).array();
        return ByteString.copyFrom(bytes);
    }

    @Test
    void getSpanId_validBytes() {
        ByteString bytes = longToByteString(12345L);

        long result = OtlpTraceMapperUtils.getSpanId(bytes);

        assertThat(result).isEqualTo(12345L);
    }

    @Test
    void getSpanId_maxValue() {
        ByteString bytes = longToByteString(Long.MAX_VALUE);

        long result = OtlpTraceMapperUtils.getSpanId(bytes);

        assertThat(result).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void getSpanId_negativeValue() {
        ByteString bytes = longToByteString(-1L);

        long result = OtlpTraceMapperUtils.getSpanId(bytes);

        assertThat(result).isEqualTo(-1L);
    }

    @Test
    void getSpanId_empty_throws() {
        assertThatThrownBy(() -> OtlpTraceMapperUtils.getSpanId(ByteString.EMPTY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found spanId");
    }

    @Test
    void getSpanId_null_throws() {
        assertThatThrownBy(() -> OtlpTraceMapperUtils.getSpanId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found spanId");
    }

    // =======================================================================
    // getParentSpanId(ByteString)
    // =======================================================================

    @Test
    void getParentSpanId_validBytes() {
        ByteString bytes = longToByteString(67890L);

        long result = OtlpTraceMapperUtils.getParentSpanId(bytes);

        assertThat(result).isEqualTo(67890L);
    }

    @Test
    void getParentSpanId_empty_returnsMinusOne() {
        long result = OtlpTraceMapperUtils.getParentSpanId(ByteString.EMPTY);

        assertThat(result).isEqualTo(-1L);
    }

    @Test
    void getParentSpanId_null_returnsMinusOne() {
        long result = OtlpTraceMapperUtils.getParentSpanId(null);

        assertThat(result).isEqualTo(-1L);
    }

    @Test
    void getSpanId_and_getParentSpanId_sameInput_sameResult() {
        ByteString bytes = longToByteString(999999L);

        assertThat(OtlpTraceMapperUtils.getSpanId(bytes))
                .isEqualTo(OtlpTraceMapperUtils.getParentSpanId(bytes));
    }

    // =======================================================================
    // toAttributeValue(AnyValue)
    // =======================================================================

    @Test
    void toAttributeValue_stringValue_returnsStringType() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(strVal("hello"));

        assertThat(result.getType()).isEqualTo(AttributeValueType.STRING);
        assertThat(result.getValue()).isEqualTo("hello");
    }

    @Test
    void toAttributeValue_boolValue_returnsBooleanType() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(boolVal(true));

        assertThat(result.getType()).isEqualTo(AttributeValueType.BOOLEAN);
        assertThat(result.getValue()).isEqualTo(true);
    }

    @Test
    void toAttributeValue_intValue_returnsLongType() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(intVal(42L));

        assertThat(result.getType()).isEqualTo(AttributeValueType.LONG);
        assertThat(result.getValue()).isEqualTo(42L);
    }

    @Test
    void toAttributeValue_doubleValue_returnsDoubleType() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(doubleVal(2.718));

        assertThat(result.getType()).isEqualTo(AttributeValueType.DOUBLE);
        assertThat(result.getValue()).isEqualTo(2.718);
    }

    @Test
    void toAttributeValue_nonEmptyBytes_returnsBytesType() {
        byte[] payload = new byte[]{1, 2, 3};

        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(bytesVal(payload));

        assertThat(result.getType()).isEqualTo(AttributeValueType.BYTES);
        assertThat((byte[]) result.getValue()).containsExactly(1, 2, 3);
    }

    @Test
    void toAttributeValue_emptyBytes_returnsBytesType() {
        // regression: empty ByteString must stay BYTES (was previously STRING)
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(bytesVal(new byte[0]));

        assertThat(result.getType()).isEqualTo(AttributeValueType.BYTES);
        assertThat((byte[]) result.getValue()).isEmpty();
    }

    @Test
    void toAttributeValue_arrayValue_returnsArrayType() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(
                arrayVal(strVal("a"), intVal(1L), boolVal(false)));

        assertThat(result.getType()).isEqualTo(AttributeValueType.ARRAY);
        @SuppressWarnings("unchecked")
        List<AttributeValue> list = (List<AttributeValue>) result.getValue();
        assertThat(list).hasSize(3);
        assertThat(list.get(0).getType()).isEqualTo(AttributeValueType.STRING);
        assertThat(list.get(0).getValue()).isEqualTo("a");
        assertThat(list.get(1).getType()).isEqualTo(AttributeValueType.LONG);
        assertThat(list.get(1).getValue()).isEqualTo(1L);
        assertThat(list.get(2).getType()).isEqualTo(AttributeValueType.BOOLEAN);
        assertThat(list.get(2).getValue()).isEqualTo(false);
    }

    @Test
    void toAttributeValue_nestedArray_preservesStructure() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(
                arrayVal(arrayVal(strVal("x"), strVal("y")), intVal(9L)));

        assertThat(result.getType()).isEqualTo(AttributeValueType.ARRAY);
        @SuppressWarnings("unchecked")
        List<AttributeValue> outer = (List<AttributeValue>) result.getValue();
        assertThat(outer).hasSize(2);
        assertThat(outer.get(0).getType()).isEqualTo(AttributeValueType.ARRAY);
        @SuppressWarnings("unchecked")
        List<AttributeValue> inner = (List<AttributeValue>) outer.get(0).getValue();
        assertThat(inner).extracting(this::getStringValue).containsExactly("x", "y");
    }

    private String getStringValue(AttributeValue s) {
        return (String) s.getValue();
    }

    private Object getObjectValue(AttributeValue s) {
        return s.getValue();
    }

    @Test
    void toAttributeValue_kvlistValue_returnsKeyValueListType() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(
                kvlistVal(kv("name", strVal("bob")), kv("age", intVal(30L))));

        assertThat(result.getType()).isEqualTo(AttributeValueType.KEY_VALUE_LIST);
        @SuppressWarnings("unchecked")
        List<AttributeKeyValue> kvList = (List<AttributeKeyValue>) result.getValue();
        assertThat(kvList).hasSize(2);
        assertThat(kvList.get(0).getKey()).isEqualTo("name");
        assertThat(kvList.get(0).getValue().getValue()).isEqualTo("bob");
        assertThat(kvList.get(1).getKey()).isEqualTo("age");
        assertThat(kvList.get(1).getValue().getValue()).isEqualTo(30L);
    }

    @Test
    void toAttributeValue_kvlistContainingArray_preservesStructure() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(
                kvlistVal(kv("tags", arrayVal(strVal("t1"), strVal("t2")))));

        assertThat(result.getType()).isEqualTo(AttributeValueType.KEY_VALUE_LIST);
        @SuppressWarnings("unchecked")
        List<AttributeKeyValue> kvList = (List<AttributeKeyValue>) result.getValue();
        assertThat(kvList).hasSize(1);
        AttributeValue nested = kvList.get(0).getValue();
        assertThat(nested.getType()).isEqualTo(AttributeValueType.ARRAY);
        @SuppressWarnings("unchecked")
        List<AttributeValue> innerList = (List<AttributeValue>) nested.getValue();
        assertThat(innerList).extracting(this::getObjectValue).containsExactly("t1", "t2");
    }

    @Test
    void toAttributeValue_unsetField_returnsNull() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(AnyValue.getDefaultInstance());

        assertThat(result).isNull();
    }

    @Test
    void toAttributeValue_arrayWithUnsetItem_skipsNull() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(
                arrayVal(strVal("keep"), AnyValue.getDefaultInstance(), intVal(1L)));

        assertThat(result.getType()).isEqualTo(AttributeValueType.ARRAY);
        @SuppressWarnings("unchecked")
        List<AttributeValue> list = (List<AttributeValue>) result.getValue();
        assertThat(list).hasSize(2);
        assertThat(list).extracting(this::getObjectValue).containsExactly("keep", 1L);
    }

    @Test
    void toAttributeValue_kvlistWithUnsetValue_skipsEntry() {
        AttributeValue result = OtlpTraceMapperUtils.toAttributeValue(
                kvlistVal(
                        kv("keep", strVal("ok")),
                        kv("drop", AnyValue.getDefaultInstance())));

        assertThat(result.getType()).isEqualTo(AttributeValueType.KEY_VALUE_LIST);
        @SuppressWarnings("unchecked")
        List<AttributeKeyValue> kvList = (List<AttributeKeyValue>) result.getValue();
        assertThat(kvList).hasSize(1);
        assertThat(kvList.get(0).getKey()).isEqualTo("keep");
    }

    // =======================================================================
    // getAttributeValueMap(List<KeyValue>)
    // =======================================================================

    @Test
    void getAttributeValueMap_emptyList_returnsEmptyMap() {
        Map<String, AttributeValue> result = OtlpTraceMapperUtils.getAttributeValueMap(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void getAttributeValueMap_mixedTypes_preservesTypes() {
        List<KeyValue> attrs = List.of(
                kv("s", strVal("v")),
                kv("i", intVal(5L)),
                kv("b", boolVal(true)),
                kv("d", doubleVal(1.5)),
                kv("bytes", bytesVal(new byte[]{9, 8, 7}))
        );

        Map<String, AttributeValue> result = OtlpTraceMapperUtils.getAttributeValueMap(attrs);

        assertThat(result).containsOnlyKeys("s", "i", "b", "d", "bytes");
        assertThat(result.get("s").getType()).isEqualTo(AttributeValueType.STRING);
        assertThat(result.get("i").getType()).isEqualTo(AttributeValueType.LONG);
        assertThat(result.get("b").getType()).isEqualTo(AttributeValueType.BOOLEAN);
        assertThat(result.get("d").getType()).isEqualTo(AttributeValueType.DOUBLE);
        assertThat(result.get("bytes").getType()).isEqualTo(AttributeValueType.BYTES);
        assertThat((byte[]) result.get("bytes").getValue()).containsExactly(9, 8, 7);
    }

    // =======================================================================
    // toAttributeBoList(Map<String, AttributeValue>, Predicate)
    // =======================================================================

    @Test
    void toAttributeBoList_emptyMap_returnsEmptyList() {
        List<AttributeBo> result = OtlpTraceMapperUtils.toAttributeBoList(Map.of(), key -> false);

        assertThat(result).isEmpty();
    }

    @Test
    void toAttributeBoList_noFilter_returnsAllEntries() {
        Map<String, AttributeValue> attrs = new HashMap<>();
        attrs.put("k1", AttributeValue.of("v1"));
        attrs.put("k2", AttributeValue.of(42L));

        List<AttributeBo> result = OtlpTraceMapperUtils.toAttributeBoList(attrs, key -> false);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AttributeBo::getKey).containsExactlyInAnyOrder("k1", "k2");
    }

    @Test
    void toAttributeBoList_excludeFilter_removesKeys() {
        Map<String, AttributeValue> attrs = new HashMap<>();
        attrs.put("keep", AttributeValue.of("ok"));
        attrs.put("drop", AttributeValue.of("bye"));

        List<AttributeBo> result = OtlpTraceMapperUtils.toAttributeBoList(
                attrs, key -> key.equals("drop"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey()).isEqualTo("keep");
        assertThat(result.get(0).getValue().getValue()).isEqualTo("ok");
    }

    // =======================================================================
    // toAttributeBoList(Map, Predicate, TransformContext) — single-pass truncation
    // =======================================================================

    private static List<AttributeBo> truncate(TransformContext context, AttributeValue value) {
        final Map<String, AttributeValue> attrs = new LinkedHashMap<>();
        attrs.put("k", value);
        return OtlpTraceMapperUtils.toAttributeBoList(attrs, key -> false, context);
    }

    @Test
    void truncateAttributeValues_emptyMap_returnsZero() {
        TransformContext context = new TransformContext(8);

        List<AttributeBo> result = OtlpTraceMapperUtils.toAttributeBoList(Map.of(), key -> false, context);

        assertThat(result).isEmpty();
        assertThat(context.truncatedCount()).isZero();
    }

    @Test
    void truncateAttributeValues_shortString_unchanged() {
        TransformContext context = new TransformContext(64);

        List<AttributeBo> list = truncate(context, AttributeValue.of("ok"));

        assertThat(context.truncatedCount()).isZero();
        assertThat(list.get(0).getValue().getValue()).isEqualTo("ok");
    }

    @Test
    void truncateAttributeValues_overLongString_truncatedAndCounted() {
        TransformContext context = new TransformContext(10);

        List<AttributeBo> list = truncate(context, AttributeValue.of("a".repeat(100)));

        assertThat(context.truncatedCount()).isEqualTo(1);
        AttributeValue value = list.get(0).getValue();
        assertThat(value.getType()).isEqualTo(AttributeValueType.STRING);
        assertThat((String) value.getValue()).isEqualTo("a".repeat(10));
    }

    @Test
    void truncateAttributeValues_numericAndBoolean_neverTruncated() {
        TransformContext context = new TransformContext(1);
        Map<String, AttributeValue> attrs = new LinkedHashMap<>();
        attrs.put("i", AttributeValue.of(1234567890L));
        attrs.put("d", AttributeValue.of(3.14159d));
        attrs.put("b", AttributeValue.of(true));

        // maxBytes=1: would truncate any string, but numbers/booleans are exempt per OTel spec
        List<AttributeBo> list = OtlpTraceMapperUtils.toAttributeBoList(attrs, key -> false, context);

        assertThat(context.truncatedCount()).isZero();
        assertThat(list.get(0).getValue().getValue()).isEqualTo(1234567890L);
        assertThat(list.get(1).getValue().getValue()).isEqualTo(3.14159d);
        assertThat(list.get(2).getValue().getValue()).isEqualTo(true);
    }

    @Test
    void truncateAttributeValues_overLongBytes_truncatedToMaxBytes() {
        byte[] payload = new byte[100];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) i;
        }
        TransformContext context = new TransformContext(16);

        List<AttributeBo> list = truncate(context, AttributeValue.of(payload));

        assertThat(context.truncatedCount()).isEqualTo(1);
        AttributeValue value = list.get(0).getValue();
        assertThat(value.getType()).isEqualTo(AttributeValueType.BYTES);
        assertThat((byte[]) value.getValue()).hasSize(16)
                .containsExactly(java.util.Arrays.copyOf(payload, 16));
    }

    @Test
    void truncateAttributeValues_bytesWithinLimit_unchanged() {
        byte[] payload = new byte[]{1, 2, 3};
        TransformContext context = new TransformContext(16);

        List<AttributeBo> list = truncate(context, AttributeValue.of(payload));

        assertThat(context.truncatedCount()).isZero();
        assertThat((byte[]) list.get(0).getValue().getValue()).containsExactly(1, 2, 3);
    }

    @Test
    void truncateAttributeValues_array_recursesAndCountsEachLeaf() {
        AttributeValue array = AttributeValue.of(
                AttributeValue.of("a".repeat(100)),
                AttributeValue.of("short"),
                AttributeValue.of("b".repeat(100))
        );
        TransformContext context = new TransformContext(10);

        List<AttributeBo> list = truncate(context, array);

        // two over-long leaves truncated, the "short" one untouched
        assertThat(context.truncatedCount()).isEqualTo(2);
        @SuppressWarnings("unchecked")
        List<AttributeValue> result = (List<AttributeValue>) list.get(0).getValue().getValue();
        assertThat(result).extracting(AttributeValue::getValue)
                .containsExactly("a".repeat(10), "short", "b".repeat(10));
    }

    @Test
    void truncateAttributeValues_array_noOverLongLeaf_unchanged() {
        AttributeValue array = AttributeValue.of(AttributeValue.of("x"), AttributeValue.of("y"));
        TransformContext context = new TransformContext(10);

        List<AttributeBo> list = truncate(context, array);

        assertThat(context.truncatedCount()).isZero();
        @SuppressWarnings("unchecked")
        List<AttributeValue> result = (List<AttributeValue>) list.get(0).getValue().getValue();
        assertThat(result).extracting(AttributeValue::getValue).containsExactly("x", "y");
    }

    @Test
    void truncateAttributeValues_keyValueList_recurses() {
        AttributeValue kvList = AttributeValue.ofAttributeKeyValueList(
                AttributeKeyValue.of("big", AttributeValue.of("a".repeat(100))),
                AttributeKeyValue.of("small", AttributeValue.of("ok"))
        );
        TransformContext context = new TransformContext(10);

        List<AttributeBo> list = truncate(context, kvList);

        assertThat(context.truncatedCount()).isEqualTo(1);
        @SuppressWarnings("unchecked")
        List<AttributeKeyValue> result = (List<AttributeKeyValue>) list.get(0).getValue().getValue();
        assertThat(result.get(0).getKey()).isEqualTo("big");
        assertThat(result.get(0).getValue().getValue()).isEqualTo("a".repeat(10));
        assertThat(result.get(1).getValue().getValue()).isEqualTo("ok");
    }

    @Test
    void truncateAttributeValues_nestedArrayInsideKeyValueList_recursesDeeply() {
        AttributeValue kvList = AttributeValue.ofAttributeKeyValueList(
                AttributeKeyValue.of("tags", AttributeValue.of(
                        AttributeValue.of("a".repeat(100)),
                        AttributeValue.of("b".repeat(100))))
        );
        TransformContext context = new TransformContext(10);

        List<AttributeBo> list = truncate(context, kvList);

        assertThat(context.truncatedCount()).isEqualTo(2);
        @SuppressWarnings("unchecked")
        List<AttributeKeyValue> outer = (List<AttributeKeyValue>) list.get(0).getValue().getValue();
        @SuppressWarnings("unchecked")
        List<AttributeValue> inner = (List<AttributeValue>) outer.get(0).getValue().getValue();
        assertThat(inner).extracting(AttributeValue::getValue)
                .containsExactly("a".repeat(10), "b".repeat(10));
    }

    @Test
    void truncateAttributeValues_multipleBos_sumsTruncations() {
        TransformContext context = new TransformContext(10);
        Map<String, AttributeValue> attrs = new LinkedHashMap<>();
        attrs.put("a", AttributeValue.of("a".repeat(100)));
        attrs.put("b", AttributeValue.of("ok"));
        attrs.put("c", AttributeValue.of("c".repeat(100)));

        List<AttributeBo> list = OtlpTraceMapperUtils.toAttributeBoList(attrs, key -> false, context);

        assertThat(context.truncatedCount()).isEqualTo(2);
        assertThat(list.get(0).getValue().getValue()).isEqualTo("a".repeat(10));
        assertThat(list.get(1).getValue().getValue()).isEqualTo("ok");
        assertThat(list.get(2).getValue().getValue()).isEqualTo("c".repeat(10));
    }

    // =======================================================================
    // getAttributeToMap(List<KeyValue>, TransformContext) — single-pass truncation
    // =======================================================================

    @SuppressWarnings("unchecked")
    @Test
    void truncatingTransform_overLongString_truncatedAndCounted() {
        TransformContext context = new TransformContext(10);

        Map<String, Object> map = OtlpTraceMapperUtils.getAttributeToMap(List.of(
                kv("big", strVal("a".repeat(100))),
                kv("small", strVal("ok"))
        ), context);

        assertThat(context.truncatedCount()).isEqualTo(1);
        assertThat(map).containsEntry("big", "a".repeat(10));
        assertThat(map).containsEntry("small", "ok");
    }

    @Test
    void truncatingTransform_nonStringValues_untouched() {
        TransformContext context = new TransformContext(1);

        Map<String, Object> map = OtlpTraceMapperUtils.getAttributeToMap(List.of(
                kv("i", intVal(1234567890L)),
                kv("b", boolVal(true)),
                kv("d", doubleVal(3.14d))
        ), context);

        assertThat(context.truncatedCount()).isZero();
        assertThat(map).containsEntry("i", 1234567890L);
        assertThat(map).containsEntry("b", true);
        assertThat(map).containsEntry("d", 3.14d);
    }

    @SuppressWarnings("unchecked")
    @Test
    void truncatingTransform_nestedMapAndList_recurses() {
        TransformContext context = new TransformContext(10);

        Map<String, Object> map = OtlpTraceMapperUtils.getAttributeToMap(List.of(
                kv("map", kvlistVal(kv("inner", strVal("a".repeat(100))))),
                kv("list", arrayVal(strVal("b".repeat(100)), strVal("ok")))
        ), context);

        assertThat(context.truncatedCount()).isEqualTo(2);
        assertThat((Map<String, Object>) map.get("map")).containsEntry("inner", "a".repeat(10));
        assertThat((List<Object>) map.get("list")).containsExactly("b".repeat(10), "ok");
    }

    @Test
    void truncatingTransform_withinLimit_countsZero() {
        TransformContext context = new TransformContext(64);

        Map<String, Object> map = OtlpTraceMapperUtils.getAttributeToMap(List.of(
                kv("k", strVal("short"))
        ), context);

        assertThat(context.truncatedCount()).isZero();
        assertThat(map).containsEntry("k", "short");
    }

    @SuppressWarnings("unchecked")
    @Test
    void truncatingTransform_arrayOverLongStrings_truncatedInPlace() {
        TransformContext context = new TransformContext(10);

        Map<String, Object> map = OtlpTraceMapperUtils.getAttributeToMap(List.of(
                kv("list", arrayVal(strVal("a".repeat(100)), strVal("ok"), strVal("b".repeat(100))))
        ), context);

        assertThat(context.truncatedCount()).isEqualTo(2);
        assertThat((List<Object>) map.get("list")).containsExactly("a".repeat(10), "ok", "b".repeat(10));
    }

    @Test
    void truncatingTransform_bytesWithinLimit_notTruncated() {
        TransformContext context = new TransformContext(64);

        Map<String, Object> map = OtlpTraceMapperUtils.getAttributeToMap(List.of(
                kv("bin", bytesVal(new byte[]{0x0a, (byte) 0xbc})) // 2 bytes -> 4 hex chars
        ), context);

        assertThat(context.truncatedCount()).isZero();
        assertThat((String) map.get("bin")).hasSize(4);
    }

    @Test
    void truncatingTransform_bytesHexString_truncated() {
        // BYTES values are rendered as a hex string and subject to the same cap.
        TransformContext context = new TransformContext(10);

        Map<String, Object> map = OtlpTraceMapperUtils.getAttributeToMap(List.of(
                kv("bin", bytesVal(new byte[16])) // 16 bytes -> 32 hex chars, exceeds 10
        ), context);

        assertThat(context.truncatedCount()).isEqualTo(1);
        assertThat((String) map.get("bin")).hasSize(10);
    }

}