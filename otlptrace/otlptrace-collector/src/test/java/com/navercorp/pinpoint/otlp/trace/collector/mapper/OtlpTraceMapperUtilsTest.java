package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueType;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
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
        assertThat(result.agentName()).isNull();
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

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.agentId()).isEqualTo("fallback-app");
        assertThat(result.applicationName()).isEqualTo("fallback-app");
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
    void getId_pinpointApplicationName_hasPriority() {
        Map<String, AttributeValue> attrs = Map.of(
                "pinpoint.agentId", AttributeValue.of("agent1"),
                "pinpoint.applicationName", AttributeValue.of("pinpoint-app"),
                "service.name", AttributeValue.of("otel-service")
        );

        IdAndName result = OtlpTraceMapperUtils.getId(attrs);

        assertThat(result.applicationName()).isEqualTo("pinpoint-app");
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
        assertThat(inner).extracting(AttributeValue::getValue).containsExactly("x", "y");
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
        assertThat(innerList).extracting(AttributeValue::getValue).containsExactly("t1", "t2");
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
        assertThat(list).extracting(AttributeValue::getValue).containsExactly("keep", 1L);
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

}