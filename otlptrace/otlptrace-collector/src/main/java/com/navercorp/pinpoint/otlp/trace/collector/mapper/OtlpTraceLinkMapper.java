package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import com.navercorp.pinpoint.common.server.util.Utf8;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.trace.v1.Span;
import org.apache.commons.io.output.StringBuilderWriter;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils.writeAttributes;

@Component
public class OtlpTraceLinkMapper {

    private static final String FIELD_TRACE_ID = "traceId";
    private static final String FIELD_SPAN_ID = "spanId";
    private static final String FIELD_TRACE_STATE = "traceState";
    private static final String FIELD_ATTRIBUTES = "attributes";
    private static final String FIELD_DROPPED = "dropped";

    private final ObjectMapper objectMapper;
    private final int valueMaxBytes;

    public OtlpTraceLinkMapper(ObjectMapper objectMapper,
                               @Value("${pinpoint.collector.otlptrace.link.value-max-bytes:8192}") int valueMaxBytes) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        if (valueMaxBytes < 0) {
            throw new IllegalArgumentException("valueMaxBytes must be >= 0: " + valueMaxBytes);
        }
        this.valueMaxBytes = valueMaxBytes;
    }

    /**
     * Serializes an OTel {@link Span.Link} as {@link OtelLink} JSON into an
     * {@link AnnotationKey#OPENTELEMETRY_LINK} annotation.
     *
     * <p>Returns {@code null} when the link does not carry a valid SpanContext (16-byte non-zero
     * traceId + 8-byte non-zero spanId). OTel requires a link to reference a valid span, so an
     * invalid/empty link is unusable input; only the link is dropped, not the owning span.</p>
     *
     * <p>{@code onTruncated} is invoked once per truncated value (traceState or attribute leaf).</p>
     */
    public @Nullable AnnotationBo toAnnotation(Span.Link link, TruncationListener onTruncated) {
        if (!OtlpIdValidator.isValidTraceId(link.getTraceId()) || !OtlpIdValidator.isValidSpanId(link.getSpanId())) {
            return null;
        }
        try {
            final OtelLink otelLink = toOtelLink(link);
            final String value = toJson(otelLink, onTruncated);
            return AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), value);
        } catch (JacksonException e) {
            return AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), "json processing error");
        }
    }

    private String toJson(OtelLink otelLink, TruncationListener onTruncated) {
        final StringBuilderWriter buffer = new StringBuilderWriter();
        try (JsonGenerator generator = objectMapper.createGenerator(buffer)) {
            generator.writeStartObject();
            if (!otelLink.traceId().isEmpty()) {
                generator.writeStringProperty(FIELD_TRACE_ID, ByteStringUtils.encodeBase16(otelLink.traceId()));
            }
            if (otelLink.spanId() != 0) {
                generator.writeStringProperty(FIELD_SPAN_ID, String.valueOf(otelLink.spanId()));
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
                generator.writeStringProperty(FIELD_TRACE_STATE, traceState);
            }
            if (!otelLink.attributes().isEmpty()) {
                // Attribute values are capped at write time, invoking onTruncated per truncated leaf.
                generator.writeName(FIELD_ATTRIBUTES);
                writeAttributes(generator, otelLink.attributes(), valueMaxBytes, onTruncated);
            }
            if (otelLink.dropped() > 0) {
                generator.writeNumberProperty(FIELD_DROPPED, otelLink.dropped());
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
