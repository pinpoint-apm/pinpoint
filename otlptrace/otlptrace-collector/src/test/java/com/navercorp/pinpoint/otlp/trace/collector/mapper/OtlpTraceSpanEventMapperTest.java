package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceSpanEventMapperTest {

    private static final ByteString TRACE_ID = ByteString.copyFrom(new byte[]{
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10});
    private static final String TRACE_ID_HEX = "0102030405060708090a0b0c0d0e0f10";

    private static final ByteString LINKED_SPAN_ID = ByteString.copyFrom(new byte[]{
            0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88});
    private static final String LINKED_SPAN_ID_HEX = "1122334455667788";

    private static final ByteString OTHER_LINKED_SPAN_ID = ByteString.copyFrom(new byte[]{
            (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD,
            (byte) 0xEE, (byte) 0xFF, 0x00, 0x11});
    private static final String OTHER_LINKED_SPAN_ID_HEX = "aabbccddeeff0011";

    private static final ByteString OWN_SPAN_ID = ByteString.copyFrom(new byte[]{
            0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77});

    private OtlpTraceSpanEventMapper mapper;
    private ObjectMapper om;

    @BeforeEach
    void setUp() {
        om = new ObjectMapper();
        mapper = new OtlpTraceSpanEventMapper(
                new OtlpTraceEventMapper(om),
                new OtlpTraceLinkMapper(om));
    }

    private Span internalSpan(Span.Link... links) {
        Span.Builder b = Span.newBuilder()
                .setName("do-work")
                .setKind(Span.SpanKind.SPAN_KIND_INTERNAL)
                .setSpanId(OWN_SPAN_ID)
                .setStartTimeUnixNano(1_000_000L)
                .setEndTimeUnixNano(2_000_000L);
        for (Span.Link link : links) {
            b.addLinks(link);
        }
        return b.build();
    }

    private List<AnnotationBo> linkAnnotations(SpanEventBo spanEventBo) {
        return spanEventBo.getAnnotationBoList().stream()
                .filter(a -> a.getKey() == AnnotationKey.OPENTELEMETRY_LINK.getCode())
                .toList();
    }

    @Test
    void internalSpan_withLink_writesLinkAnnotation() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(LINKED_SPAN_ID)
                .build();

        SpanEventBo result = mapper.map(0L, internalSpan(link), 1);

        List<AnnotationBo> linkAnns = linkAnnotations(result);
        assertThat(linkAnns).hasSize(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = om.readValue((String) linkAnns.get(0).getValue(), Map.class);
        assertThat(payload).containsEntry("traceId", TRACE_ID_HEX);
        assertThat(payload).containsEntry("spanId", LINKED_SPAN_ID_HEX);
    }

    @Test
    void internalSpan_withMultipleLinks_writesEachAsAnnotation() throws Exception {
        Span.Link first = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(LINKED_SPAN_ID)
                .build();
        Span.Link second = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(OTHER_LINKED_SPAN_ID)
                .build();

        SpanEventBo result = mapper.map(0L, internalSpan(first, second), 1);

        List<AnnotationBo> linkAnns = linkAnnotations(result);
        assertThat(linkAnns).hasSize(2);
        @SuppressWarnings("unchecked")
        Map<String, Object> p0 = om.readValue((String) linkAnns.get(0).getValue(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> p1 = om.readValue((String) linkAnns.get(1).getValue(), Map.class);
        assertThat(p0).containsEntry("spanId", LINKED_SPAN_ID_HEX);
        assertThat(p1).containsEntry("spanId", OTHER_LINKED_SPAN_ID_HEX);
    }

    @Test
    void internalSpan_withEventsAndLinks_writesBoth() {
        Span.Event event = Span.Event.newBuilder()
                .setName("checkpoint")
                .build();
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(LINKED_SPAN_ID)
                .build();
        Span span = Span.newBuilder()
                .setName("do-work")
                .setKind(Span.SpanKind.SPAN_KIND_INTERNAL)
                .setSpanId(OWN_SPAN_ID)
                .setStartTimeUnixNano(1_000_000L)
                .setEndTimeUnixNano(2_000_000L)
                .addEvents(event)
                .addLinks(link)
                .build();

        SpanEventBo result = mapper.map(0L, span, 1);

        List<AnnotationBo> anns = result.getAnnotationBoList();
        assertThat(anns).anyMatch(a -> a.getKey() == AnnotationKey.OPENTELEMETRY_EVENT.getCode());
        assertThat(anns).anyMatch(a -> a.getKey() == AnnotationKey.OPENTELEMETRY_LINK.getCode());
    }

    @Test
    void internalSpan_noLinks_writesNoLinkAnnotation() {
        SpanEventBo result = mapper.map(0L, internalSpan(), 1);

        assertThat(linkAnnotations(result)).isEmpty();
    }

    @Test
    void internalSpan_malformedLink_isSkipped() {
        Span.Link missingSpanId = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .build();

        SpanEventBo result = mapper.map(0L, internalSpan(missingSpanId), 1);

        assertThat(linkAnnotations(result)).isEmpty();
    }

    @Test
    void internalSpan_linkWithAttributes_preservesAttributes() throws Exception {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(LINKED_SPAN_ID)
                .addAttributes(kv("link.kind", strVal("follows_from")))
                .build();

        SpanEventBo result = mapper.map(0L, internalSpan(link), 1);

        List<AnnotationBo> linkAnns = linkAnnotations(result);
        assertThat(linkAnns).hasSize(1);
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = om.readValue((String) linkAnns.get(0).getValue(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> attrs = (Map<String, Object>) payload.get("attributes");
        assertThat(attrs).containsEntry("link.kind", "follows_from");
    }

    @Test
    void clientSpan_withLink_writesLinkAnnotation() {
        Span.Link link = Span.Link.newBuilder()
                .setTraceId(TRACE_ID)
                .setSpanId(LINKED_SPAN_ID)
                .build();
        Span span = Span.newBuilder()
                .setName("http-call")
                .setKind(Span.SpanKind.SPAN_KIND_CLIENT)
                .setSpanId(OWN_SPAN_ID)
                .setStartTimeUnixNano(1_000_000L)
                .setEndTimeUnixNano(2_000_000L)
                .addLinks(link)
                .build();

        SpanEventBo result = mapper.map(0L, span, 1);

        assertThat(linkAnnotations(result)).hasSize(1);
    }
}
