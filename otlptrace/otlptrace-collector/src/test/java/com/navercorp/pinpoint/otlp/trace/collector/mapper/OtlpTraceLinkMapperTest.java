package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
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

    private static final byte[] TRACE_ID = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
    };
    private static final byte[] SPAN_ID = {1, 2, 3, 4, 5, 6, 7, 8};

    private OtlpTraceLinkMapper mapper;
    private List<AnnotationBo> annotations;
    private AnnotationWriter writer;

    @BeforeEach
    void setUp() {
        mapper = new OtlpTraceLinkMapper(new ObjectMapper(), 8192);
        annotations = new ArrayList<>();
        writer = annotations::add;
    }

    @Test
    void minimal_link_writesTraceIdAndSpanId() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .build();

        mapper.addLinkToAnnotation(link, writer, () -> {});

        assertThat(annotations).hasSize(1);
        AnnotationBo bo = annotations.get(0);
        assertThat(bo.getKey()).isEqualTo(AnnotationKey.OPENTELEMETRY_LINK.getCode());

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) bo.getValue(), Map.class);
        assertThat(root).containsKey("traceId");
        assertThat(root).containsKey("spanId");
        assertThat(root).doesNotContainKey("traceState");
        assertThat(root).doesNotContainKey("attributes");
        assertThat(root).doesNotContainKey("dropped");
    }

    @Test
    void traceState_emittedWhenNonEmpty() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setTraceState("aws=t61rcWkgMzE,dd=s:1;o:rum")
                .build();

        mapper.addLinkToAnnotation(link, writer, () -> {});

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        assertThat(root).containsEntry("traceState", "aws=t61rcWkgMzE,dd=s:1;o:rum");
    }

    @Test
    void traceState_omittedWhenEmpty() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .build();

        mapper.addLinkToAnnotation(link, writer, () -> {});

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        assertThat(root).doesNotContainKey("traceState");
    }

    @Test
    void attributes_emittedWhenPresent() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .addAttributes(kv("link.kind", strVal("follows_from")))
                .build();

        mapper.addLinkToAnnotation(link, writer, () -> {});

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> attrs = (Map<String, Object>) root.get("attributes");
        assertThat(attrs).containsEntry("link.kind", "follows_from");
    }

    @Test
    void dropped_emittedWhenNonZero() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setDroppedAttributesCount(5)
                .build();

        mapper.addLinkToAnnotation(link, writer, () -> {});

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        assertThat(root).containsEntry("dropped", 5);
    }

    @Test
    void dropped_omittedWhenZero() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .build();

        mapper.addLinkToAnnotation(link, writer, () -> {});

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        assertThat(root).doesNotContainKey("dropped");
    }

    @Test
    void emptyLink_isSkipped() {
        // Both traceId and spanId empty — OTel spec violation, skip silently.
        Span.Link link = Span.Link.newBuilder().build();

        mapper.addLinkToAnnotation(link, writer, () -> {});

        assertThat(annotations).isEmpty();
    }

    @Test
    void emptyLink_withAttributesOnly_isSkipped() {
        // Even if attributes are populated, an empty SpanContext means the link can't be
        // resolved to a real span — skip rather than emit a useless annotation.
        Span.Link link = Span.Link.newBuilder()
                .addAttributes(kv("orphan", strVal("yes")))
                .build();

        mapper.addLinkToAnnotation(link, writer, () -> {});

        assertThat(annotations).isEmpty();
    }

    @Test
    void allFields_combined() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setTraceState("vendor=foo")
                .addAttributes(kv("link.kind", strVal("child_of")))
                .setDroppedAttributesCount(2)
                .build();

        mapper.addLinkToAnnotation(link, writer, () -> {});

        ObjectMapper om = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> root = om.readValue((String) annotations.get(0).getValue(), Map.class);
        assertThat(root).containsKeys("traceId", "spanId", "traceState", "attributes", "dropped");
        assertThat(root).containsEntry("traceState", "vendor=foo");
        assertThat(root).containsEntry("dropped", 2);
    }
}
