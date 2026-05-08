package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceLinkMapperTest {

    private static final ByteString TRACE_ID = ByteString.copyFrom(new byte[]{
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10});
    private static final String TRACE_ID_HEX = "0102030405060708090a0b0c0d0e0f10";

    private static final ByteString SPAN_ID = ByteString.copyFrom(new byte[]{
            0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88});
    private static final String SPAN_ID_HEX = "1122334455667788";

    private OtlpTraceLinkMapper mapper;
    private List<AnnotationBo> annotations;
    private AnnotationWriter writer;

    @BeforeEach
    void setUp() {
        mapper = new OtlpTraceLinkMapper(new ObjectMapper());
        annotations = new ArrayList<>();
        writer = annotations::add;
    }

    @Test
    void minimal_writesIdsAndEmptyAttributes() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .build();

        mapper.addLinkToAnnotation(link, writer);

        assertThat(annotations).hasSize(1);
        AnnotationBo bo = annotations.get(0);
        assertThat(bo.getKey()).isEqualTo(AnnotationKey.OPENTELEMETRY_LINK.getCode());

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) bo.getValue(), Map.class);
        assertThat(root).containsEntry("traceId", TRACE_ID_HEX);
        assertThat(root).containsEntry("spanId", SPAN_ID_HEX);
        assertThat(root.get("attributes")).isEqualTo(Map.of());
        assertThat(root).doesNotContainKeys("traceState", "flags");
    }

    @Test
    void withTraceStateAndFlags_preservesBoth() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .setTraceState("vendor=abc,other=xyz")
                .setFlags(1)
                .build();

        mapper.addLinkToAnnotation(link, writer);

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        assertThat(root).containsEntry("traceState", "vendor=abc,other=xyz");
        assertThat(root).containsEntry("flags", 1);
    }

    @Test
    void withAttributes_includesAttributeMap() throws Exception {
        List<KeyValue> attrs = List.of(
                kv("link.kind", strVal("follows_from"))
        );
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .addAllAttributes(attrs)
                .build();

        mapper.addLinkToAnnotation(link, writer);

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) root.get("attributes");
        assertThat(attributes).containsEntry("link.kind", "follows_from");
    }

    @Test
    void missingTraceId_skipsAnnotation() {
        Span.Link link = Span.Link.newBuilder()
                .setSpanId(SPAN_ID)
                .build();

        mapper.addLinkToAnnotation(link, writer);

        assertThat(annotations).isEmpty();
    }

    @Test
    void missingSpanId_skipsAnnotation() {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .build();

        mapper.addLinkToAnnotation(link, writer);

        assertThat(annotations).isEmpty();
    }

    @Test
    void emptyLink_skipsAnnotation() {
        mapper.addLinkToAnnotation(Span.Link.getDefaultInstance(), writer);

        assertThat(annotations).isEmpty();
    }

    @Test
    void zeroFlags_omitsFlagsField() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .setFlags(0)
                .build();

        mapper.addLinkToAnnotation(link, writer);

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        assertThat(root).doesNotContainKey("flags");
    }

    @Test
    void emptyTraceState_omitsTraceStateField() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .build();

        mapper.addLinkToAnnotation(link, writer);

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        assertThat(root).doesNotContainKey("traceState");
    }
}
