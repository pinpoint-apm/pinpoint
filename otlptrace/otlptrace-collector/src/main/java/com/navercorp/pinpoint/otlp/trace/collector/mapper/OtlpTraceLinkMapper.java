package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import com.navercorp.pinpoint.common.server.util.Utf8;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.trace.v1.Span;
import org.apache.commons.io.output.StringBuilderWriter;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils.writeAttributes;

@Component
public class OtlpTraceLinkMapper {

    private static final String FIELD_TRACE_ID = "traceId";
    private static final String FIELD_SPAN_ID = "spanId";
    private static final String FIELD_TRACE_STATE = "traceState";
    private static final String FIELD_ATTRIBUTES = "attributes";
    private static final String FIELD_DROPPED = "dropped";

    private final JsonFactory jsonFactory;
    private final int valueMaxBytes;

    public OtlpTraceLinkMapper(ObjectMapper objectMapper,
                               @Value("${pinpoint.collector.otlptrace.link.value-max-bytes:8192}") int valueMaxBytes) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.jsonFactory = objectMapper.getFactory();
        if (valueMaxBytes < 0) {
            throw new IllegalArgumentException("valueMaxBytes must be >= 0: " + valueMaxBytes);
        }
        this.valueMaxBytes = valueMaxBytes;
    }

    /**
     * Serializes an OTel {@link Span.Link} as {@link OtelLink} JSON into an
     * {@link AnnotationKey#OPENTELEMETRY_LINK} annotation.
     *
     * <p>Returns {@code null} when both traceId and spanId are empty — OTel spec requires links
     * to carry a non-empty SpanContext, so a fully-empty link is invalid input we drop silently.</p>
     *
     * <p>{@code onTruncated} is invoked once per truncated value (traceState or attribute leaf).</p>
     */
    public @Nullable AnnotationBo toAnnotation(Span.Link link, TruncationListener onTruncated) {
        if (link.getTraceId().isEmpty() && link.getSpanId().isEmpty()) {
            return null;
        }
        try {
            final OtelLink otelLink = toOtelLink(link);
            final String value = toJson(otelLink, onTruncated);
            return AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), value);
        } catch (IOException e) {
            return AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), "json processing error");
        }
    }

    private String toJson(OtelLink otelLink, TruncationListener onTruncated) throws IOException {
        final StringBuilderWriter buffer = new StringBuilderWriter();
        try (JsonGenerator generator = jsonFactory.createGenerator(buffer)) {
            generator.writeStartObject();
            if (!otelLink.traceId().isEmpty()) {
                generator.writeStringField(FIELD_TRACE_ID, ByteStringUtils.encodeBase16(otelLink.traceId()));
            }
            if (otelLink.spanId() != 0) {
                generator.writeStringField(FIELD_SPAN_ID, String.valueOf(otelLink.spanId()));
            }
            // Only emit traceState when non-empty to keep well-behaved links compact. Capped like
            // attribute values since chained vendor entries can grow long.
            if (!otelLink.traceState().isEmpty()) {
                String traceState = otelLink.traceState();
                final String truncatedTraceState = Utf8.truncate(traceState, valueMaxBytes);
                if (truncatedTraceState != null) {
                    traceState = truncatedTraceState;
                    onTruncated.truncated();
                }
                generator.writeStringField(FIELD_TRACE_STATE, traceState);
            }
            if (!otelLink.attributes().isEmpty()) {
                // Attribute values are capped at write time, invoking onTruncated per truncated leaf.
                generator.writeFieldName(FIELD_ATTRIBUTES);
                writeAttributes(generator, otelLink.attributes(), valueMaxBytes, onTruncated);
            }
            if (otelLink.dropped() > 0) {
                generator.writeNumberField(FIELD_DROPPED, otelLink.dropped());
            }
            generator.writeEndObject();
        }
        return buffer.toString();
    }

    private OtelLink toOtelLink(Span.Link link) {
        long spanId = 0;
        if (!link.getSpanId().isEmpty()) {
            spanId = ByteStringUtils.parseLong(link.getSpanId());
        }
        return new OtelLink(link.getTraceId(), spanId, link.getTraceState(), link.getAttributesList(), link.getDroppedAttributesCount());
    }
}
