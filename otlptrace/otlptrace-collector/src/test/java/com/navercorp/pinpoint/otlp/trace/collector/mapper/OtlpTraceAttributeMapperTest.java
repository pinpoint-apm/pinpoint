package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.boolVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.doubleVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.intVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceAttributeMapperTest {

    private OtlpTraceAttributeMapper mapper;
    private List<AnnotationBo> annotations;
    private AnnotationWriter writer;

    @BeforeEach
    void setUp() {
        mapper = new OtlpTraceAttributeMapper(new ObjectMapper());
        annotations = new ArrayList<>();
        writer = annotations::add;
    }

    // -----------------------------------------------------------------------
    // empty list -> skip
    // -----------------------------------------------------------------------

    @Test
    void emptyList_skipsWrite() {
        mapper.addAttributesToAnnotation(List.of(), writer);

        assertThat(annotations).isEmpty();
    }

    // -----------------------------------------------------------------------
    // only filtered keys -> all removed, skip write
    // -----------------------------------------------------------------------

    @Test
    void onlyFilteredKeys_skipsWrite() {
        List<KeyValue> attrs = List.of(
                kv(OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE, strVal("200")),
                kv(OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH, strVal("/api/hello"))
        );

        mapper.addAttributesToAnnotation(attrs, writer);

        Assertions.assertThat(annotations).isEmpty();
    }

    // -----------------------------------------------------------------------
    // write string attribute
    // -----------------------------------------------------------------------

    @Test
    void withStringAttribute_writesJson() {
        List<KeyValue> attrs = List.of(
                kv("http.method", strVal("GET"))
        );

        mapper.addAttributesToAnnotation(attrs, writer);

        assertThat(annotations).hasSize(1);
        AnnotationBo bo = annotations.get(0);
        assertThat(bo.getKey()).isEqualTo(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode());

        String json = (String) bo.getValue();
        assertThat(json).isEqualTo("{\"http.method\":\"GET\"}");
    }

    // -----------------------------------------------------------------------
    // int / bool / double attribute
    // -----------------------------------------------------------------------

    @Test
    void withMultipleAttributeTypes_writesCorrectJson() throws Exception {
        List<KeyValue> attrs = List.of(
                kv("count", intVal(42L)),
                kv("flag", boolVal(true)),
                kv("ratio", doubleVal(0.5))
        );

        mapper.addAttributesToAnnotation(attrs, writer);

        assertThat(annotations).hasSize(1);
        String json = (String) annotations.get(0).getValue();

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = om.readValue(json, Map.class);

        assertThat(map).containsEntry("count", 42);
        assertThat(map).containsEntry("flag", true);
        assertThat(map).containsEntry("ratio", 0.5);
    }

    // -----------------------------------------------------------------------
    // array attribute
    // -----------------------------------------------------------------------

    @Test
    void withArrayAttribute_writesArray() throws Exception {
        ArrayValue arrayValue = ArrayValue.newBuilder()
                .addValues(strVal("x"))
                .addValues(strVal("y"))
                .build();
        List<KeyValue> attrs = List.of(
                kv("tags", AnyValue.newBuilder().setArrayValue(arrayValue).build())
        );

        mapper.addAttributesToAnnotation(attrs, writer);

        assertThat(annotations).hasSize(1);
        String json2 = (String) annotations.get(0).getValue();

        ObjectMapper om2 = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> map2 = om2.readValue(json2, Map.class);

        assertThat(map2.get("tags")).isInstanceOf(List.class)
                .asList().containsExactly("x", "y");
    }

    // -----------------------------------------------------------------------
    // mixed keys -> filtered keys are removed, others are preserved
    // -----------------------------------------------------------------------

    @Test
    void mixedKeys_filteredKeysAreRemoved() throws Exception {
        List<KeyValue> attrs = List.of(
                kv("http.method", strVal("POST")),
                kv(OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH, strVal("/api/save")),
                kv(OtlpTraceConstants.ATTRIBUTE_KEY_DB_STATEMENT, strVal("SELECT 1"))
        );

        mapper.addAttributesToAnnotation(attrs, writer);

        assertThat(annotations).hasSize(1);
        String json = (String) annotations.get(0).getValue();

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = om.readValue(json, Map.class);

        assertThat(map).containsOnlyKeys("http.method");
        assertThat(map).containsEntry("http.method", "POST");
    }

    // -----------------------------------------------------------------------
    // verify annotation key code
    // -----------------------------------------------------------------------

    @Test
    void annotationKey_isOpentelemetryAttribute() {
        mapper.addAttributesToAnnotation(List.of(kv("k", strVal("v"))), writer);

        assertThat(annotations.get(0).getKey())
                .isEqualTo(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode());
    }

}

