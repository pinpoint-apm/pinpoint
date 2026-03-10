package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.boolVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.doubleVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.intVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceMapperUtilsTest {

    // -----------------------------------------------------------------------
    // empty list -> empty map
    // -----------------------------------------------------------------------

    @Test
    void emptyList_returnsEmptyMap() {
        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(List.of(), key -> false);

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // no keys excluded -> all entries present
    // -----------------------------------------------------------------------

    @Test
    void noExclusion_returnsAllEntries() {
        List<KeyValue> attrs = List.of(
                kv("a", strVal("1")),
                kv("b", strVal("2"))
        );

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(attrs, key -> false);

        assertThat(result).containsOnlyKeys("a", "b");
    }

    // -----------------------------------------------------------------------
    // all keys excluded -> empty map
    // -----------------------------------------------------------------------

    @Test
    void allKeysExcluded_returnsEmptyMap() {
        List<KeyValue> attrs = List.of(
                kv("x", strVal("v1")),
                kv("y", strVal("v2"))
        );

        Map<String, Object> result = OtlpTraceMapperUtils.getAttributeToMap(attrs, key -> true);

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // partial exclusion -> excluded keys are absent
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // FILTERED_ATTRIBUTE_KEY_MAP as excludeFilter
    // -----------------------------------------------------------------------

    @Test
    void filteredAttributeKeyMap_removesKnownKeys() {
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

    // -----------------------------------------------------------------------
    // value types are preserved through the filter
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // array value is preserved through the filter
    // -----------------------------------------------------------------------

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

}

