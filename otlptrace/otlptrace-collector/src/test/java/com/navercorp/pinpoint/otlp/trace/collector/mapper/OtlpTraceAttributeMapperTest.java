package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    // empty map -> skip
    // -----------------------------------------------------------------------

    @Test
    void emptyMap_skipsWrite() {
        mapper.addAttributesToAnnotation(Map.of(), writer);

        assertThat(annotations).isEmpty();
    }

    // -----------------------------------------------------------------------
    // only filtered keys -> all removed, skip write
    // -----------------------------------------------------------------------

    @Test
    void onlyFilteredKeys_skipsWrite() {
        Map<String, Object> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_RESPONSE_STATUS_CODE, "200",
                OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH, "/api/hello"
        );

        mapper.addAttributesToAnnotation(attrs, writer);

        assertThat(annotations).isEmpty();
    }

    // -----------------------------------------------------------------------
    // write string attribute
    // -----------------------------------------------------------------------

    @Test
    void withStringAttribute_writesJson() {
        Map<String, Object> attrs = Map.of(
                "http.method", "GET"
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
        Map<String, Object> attrs = Map.of(
                "count", 42L,
                "flag", true,
                "ratio", 0.5
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
    // array attribute (already converted by getAttributeToMap)
    // -----------------------------------------------------------------------

    @Test
    void withArrayAttribute_writesArray() throws Exception {
        Map<String, Object> attrs = Map.of(
                "tags", List.of("x", "y")
        );

        mapper.addAttributesToAnnotation(attrs, writer);

        assertThat(annotations).hasSize(1);
        String json = (String) annotations.get(0).getValue();

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = om.readValue(json, Map.class);

        assertThat(map.get("tags")).isInstanceOf(List.class)
                .asList().containsExactly("x", "y");
    }

    // -----------------------------------------------------------------------
    // mixed keys -> filtered keys are removed, others are preserved
    // -----------------------------------------------------------------------

    @Test
    void mixedKeys_filteredKeysAreRemoved() throws Exception {
        Map<String, Object> attrs = Map.of(
                "http.method", "POST",
                OtlpTraceConstants.ATTRIBUTE_KEY_URL_PATH, "/api/save",
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_STATEMENT, "SELECT 1"
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
    // all filtered keys in FILTERED_ATTRIBUTE_KEY_SET are removed
    // -----------------------------------------------------------------------

    @Test
    void allFilteredKeys_areRemoved() {
        Map<String, Object> attrs = new HashMap<>();
        for (String key : OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY_SET) {
            attrs.put(key, "value");
        }
        attrs.put("custom.key", "keep");

        mapper.addAttributesToAnnotation(attrs, writer);

        assertThat(annotations).hasSize(1);
        String json = (String) annotations.get(0).getValue();
        assertThat(json).contains("custom.key");
        for (String key : OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY_SET) {
            assertThat(json).doesNotContain(key);
        }
    }

    // -----------------------------------------------------------------------
    // nested map attribute
    // -----------------------------------------------------------------------

    @Test
    void withNestedMapAttribute_writesNestedJson() throws Exception {
        Map<String, Object> nested = Map.of("inner_key", "inner_value");
        Map<String, Object> attrs = Map.of("nested", nested);

        mapper.addAttributesToAnnotation(attrs, writer);

        assertThat(annotations).hasSize(1);
        String json = (String) annotations.get(0).getValue();

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = om.readValue(json, Map.class);

        assertThat(map.get("nested")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> innerMap = (Map<String, Object>) map.get("nested");
        assertThat(innerMap).containsEntry("inner_key", "inner_value");
    }

    // -----------------------------------------------------------------------
    // verify annotation key code
    // -----------------------------------------------------------------------

    @Test
    void annotationKey_isOpentelemetryAttribute() {
        mapper.addAttributesToAnnotation(Map.of("k", "v"), writer);

        assertThat(annotations.get(0).getKey())
                .isEqualTo(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode());
    }

}