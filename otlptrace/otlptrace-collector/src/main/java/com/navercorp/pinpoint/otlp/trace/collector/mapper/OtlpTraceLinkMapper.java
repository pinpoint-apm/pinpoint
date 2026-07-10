package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import com.navercorp.pinpoint.common.server.util.Utf8;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.trace.v1.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils.getAttributeToMap;

@Component
public class OtlpTraceLinkMapper {

    private final ObjectWriter mapWriter;
    private final int valueMaxBytes;

    public OtlpTraceLinkMapper(ObjectMapper objectMapper,
                               @Value("${pinpoint.collector.otlptrace.link.value-max-bytes:8192}") int valueMaxBytes) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.mapWriter = objectMapper.writerFor(new TypeReference<Map<String, Object>>() {});
        if (valueMaxBytes < 0) {
            throw new IllegalArgumentException("valueMaxBytes must be >= 0: " + valueMaxBytes);
        }
        this.valueMaxBytes = valueMaxBytes;
    }

    /**
     * Serializes an OTel {@link Span.Link} as JSON under {@link AnnotationKey#OPENTELEMETRY_LINK}.
     * Fields: {@code traceId} (hex), {@code spanId} (decimal string — see inline note),
     * {@code traceState} (W3C tracestate, only when non-empty), {@code attributes} (only when
     * non-empty), {@code dropped} (link-level dropped_attributes_count, only when > 0).
     *
     * <p>Skips entirely when both traceId and spanId are empty — OTel spec requires links to
     * carry a non-empty SpanContext, so a fully-empty link is invalid input we drop silently.</p>
     */
    public int addLinkToAnnotation(Span.Link link, AnnotationWriter annotationWriter) {
        if (link.getTraceId().isEmpty() && link.getSpanId().isEmpty()) {
            return 0;
        }
        int truncated = 0;
        try {
            Map<String, Object> map = new HashMap<>();
            if (!link.getTraceId().isEmpty()) {
                map.put("traceId", ByteStringUtils.encodeBase16(link.getTraceId()));
            }
            if (!link.getSpanId().isEmpty()) {
                // Stored as decimal String to avoid JS Number precision loss for any consumer that
                // reads the raw annotation JSON directly. Java readers (OtelLinkValue.readLong)
                // accept both number and decimal-string forms, preserving backward compatibility.
                map.put("spanId", String.valueOf(ByteStringUtils.parseLong(link.getSpanId())));
            }
            // W3C tracestate (vendor propagation context — AWS, Datadog, Sentry, etc.). Only
            // emit when non-empty to keep well-behaved links compact. Capped like attribute values
            // since chained vendor entries can grow long.
            if (!link.getTraceState().isEmpty()) {
                String traceState = link.getTraceState();
                final String truncatedTraceState = Utf8.truncate(traceState, valueMaxBytes);
                if (truncatedTraceState != null) {
                    traceState = truncatedTraceState;
                    truncated++;
                }
                map.put("traceState", traceState);
            }
            if (link.getAttributesCount() > 0) {
                final TruncationCounter counter = new TruncationCounter();
                final Map<String, Object> linkAttributes = getAttributeToMap(link.getAttributesList(), valueMaxBytes, counter);
                truncated += counter.truncatedCount();
                map.put("attributes", linkAttributes);
            }
            // SDK-side data-loss counter for link attributes. Same convention as Span/SpanEvent
            // OPENTELEMETRY_DROPPED — only included when > 0.
            if (link.getDroppedAttributesCount() > 0) {
                map.put("dropped", link.getDroppedAttributesCount());
            }
            final String value = mapWriter.writeValueAsString(map);
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), value));
        } catch (JsonProcessingException e) {
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), "json processing error"));
        }
        return truncated;
    }
}
