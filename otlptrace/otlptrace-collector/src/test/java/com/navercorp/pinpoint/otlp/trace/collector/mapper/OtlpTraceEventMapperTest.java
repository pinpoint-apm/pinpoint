package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
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

class OtlpTraceEventMapperTest {

    private OtlpTraceEventMapper mapper;
    private List<AnnotationBo> annotations;
    private AnnotationWriter writer;

    @BeforeEach
    void setUp() {
        mapper = new OtlpTraceEventMapper(new ObjectMapper());
        annotations = new ArrayList<>();
        writer = annotations::add;
    }

    // -----------------------------------------------------------------------
    // empty name -> skip
    // -----------------------------------------------------------------------

    @Test
    void emptyName() {
        Span.Event event = Span.Event.newBuilder().setName("").build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations).hasSize(1);
    }

    // -----------------------------------------------------------------------
    // name only (no attributes)
    // -----------------------------------------------------------------------

    @Test
    void nameOnly_writesEmptyStringValue() {
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                .build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations).hasSize(1);
        AnnotationBo bo = annotations.get(0);
        assertThat(bo.getKey()).isEqualTo(AnnotationKey.OPENTELEMETRY_EVENT.getCode());
        assertThat(bo.getValue()).isEqualTo("{\"exception\":\"\"}");
    }

    // -----------------------------------------------------------------------
    // name + string attribute
    // -----------------------------------------------------------------------

    @Test
    void withStringAttribute_writesAttributeMap() {
        List<KeyValue> attrs = List.of(
                kv("exception.type", strVal("java.lang.RuntimeException"))
        );
        Span.Event event = Span.Event.newBuilder()
                .setName("exception")
                .addAllAttributes(attrs)
                .build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations).hasSize(1);
        assertThat(annotations.get(0).getValue())
                .isEqualTo("{\"exception\":{\"exception.type\":\"java.lang.RuntimeException\"}}");
    }

    // -----------------------------------------------------------------------
    // name + multiple attribute types (int, bool, double)
    // -----------------------------------------------------------------------

    @Test
    void withMultipleAttributeTypes_writesCorrectJson() throws Exception {
        List<KeyValue> attrs = List.of(
                kv("count", intVal(42L)),
                kv("flag", boolVal(true)),
                kv("ratio", doubleVal(0.5))
        );
        Span.Event event = Span.Event.newBuilder()
                .setName("my-event")
                .addAllAttributes(attrs)
                .build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations).hasSize(1);
        String json = (String) annotations.get(0).getValue();

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue(json, Map.class);
        assertThat(root).containsKey("my-event");

        @SuppressWarnings("unchecked")
        Map<String, Object> inner = (Map<String, Object>) root.get("my-event");
        assertThat(inner).containsEntry("count", 42);
        assertThat(inner).containsEntry("flag", true);
        assertThat(inner).containsEntry("ratio", 0.5);
    }

    // -----------------------------------------------------------------------
    // name + array attribute
    // -----------------------------------------------------------------------

    @Test
    void withArrayAttribute_writesArray() throws Exception {
        ArrayValue arrayValue = ArrayValue.newBuilder()
                .addValues(strVal("a"))
                .addValues(strVal("b"))
                .build();
        Span.Event event = Span.Event.newBuilder()
                .setName("tagged")
                .addAttributes(kv("tags", AnyValue.newBuilder().setArrayValue(arrayValue).build()))
                .build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations).hasSize(1);
        String json = (String) annotations.get(0).getValue();

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue(json, Map.class);

        assertThat(root.get("tagged")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> inner = (Map<String, Object>) root.get("tagged");
        assertThat(inner.get("tags")).isInstanceOf(List.class)
                .asList().containsExactly("a", "b");
    }

    // -----------------------------------------------------------------------
    // verify annotation key code
    // -----------------------------------------------------------------------

    @Test
    void annotationKey_isOpentelemetryEvent() {
        Span.Event event = Span.Event.newBuilder().setName("check").build();

        mapper.addEventToAnnotation(event, writer);

        assertThat(annotations.get(0).getKey())
                .isEqualTo(AnnotationKey.OPENTELEMETRY_EVENT.getCode());
    }

}

