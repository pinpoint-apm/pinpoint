package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.boolVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.doubleVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.intVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpTraceMapperUtilsTest {

    // =======================================================================
    // getAttributeToMap(List<KeyValue>, Predicate)
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

    // =======================================================================
    // getId(Map<String, Object>)
    // =======================================================================

    @Test
    void getId_withPinpointAgentId() {
        Map<String, Object> attrs = Map.of(
                "pinpoint.agentId", "test-agent",
                "pinpoint.applicationName", "test-app"
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("test-agent");
        assertThat(result.applicationName()).isEqualTo("test-app");
        assertThat(result.agentName()).isNull();
    }

    @Test
    void getId_withServiceInstanceId_plainString() {
        Map<String, Object> attrs = Map.of(
                "service.instance.id", "my-instance",
                "service.name", "my-service"
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("my-instance");
        assertThat(result.applicationName()).isEqualTo("my-service");
    }

    @Test
    void getId_withServiceInstanceId_uuid() {
        Map<String, Object> attrs = Map.of(
                "service.instance.id", "550e8400-e29b-41d4-a716-446655440000",
                "service.name", "uuid-service"
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        // UUID should be Base64-encoded
        assertThat(result.agentId()).isNotEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(result.agentName()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(result.applicationName()).isEqualTo("uuid-service");
    }

    @Test
    void getId_withHostName() {
        Map<String, Object> attrs = Map.of(
                "host.name", "my-host",
                "service.name", "host-service"
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("my-host");
        assertThat(result.applicationName()).isEqualTo("host-service");
    }

    @Test
    void getId_fallbackToApplicationName() {
        Map<String, Object> attrs = Map.of(
                "service.name", "fallback-app"
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("fallback-app");
        assertThat(result.applicationName()).isEqualTo("fallback-app");
    }

    @Test
    void getId_noApplicationName_throws() {
        Map<String, Object> attrs = Map.of(
                "pinpoint.agentId", "agent1"
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found applicationName");
    }

    @Test
    void getId_invalidAgentId_throws() {
        Map<String, Object> attrs = Map.of(
                "pinpoint.agentId", "invalid agent id with spaces!!!",
                "pinpoint.applicationName", "test-app"
        );

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid pinpoint.agentId");
    }

    @Test
    void getId_pinpointApplicationName_hasPriority() {
        Map<String, Object> attrs = Map.of(
                "pinpoint.agentId", "agent1",
                "pinpoint.applicationName", "pinpoint-app",
                "service.name", "otel-service"
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.applicationName()).isEqualTo("pinpoint-app");
    }

    // =======================================================================
    // getApplicationName(Map<String, Object>)
    // =======================================================================

    @Test
    void getApplicationName_pinpointApplicationName() {
        Map<String, Object> attrs = Map.of(
                "pinpoint.applicationName", "my-app"
        );

        String result = OtlpTraceMapperUtils.getApplicationName(attrs);

        assertThat(result).isEqualTo("my-app");
    }

    @Test
    void getApplicationName_fallbackToServiceName() {
        Map<String, Object> attrs = Map.of(
                "service.name", "otel-service"
        );

        String result = OtlpTraceMapperUtils.getApplicationName(attrs);

        assertThat(result).isEqualTo("otel-service");
    }

    @Test
    void getApplicationName_notFound_throws() {
        Map<String, Object> attrs = Map.of();

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getApplicationName(attrs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found applicationName");
    }

    @Test
    void getApplicationName_invalidName_throws() {
        Map<String, Object> attrs = Map.of(
                "service.name", "invalid name with spaces!!!"
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

}