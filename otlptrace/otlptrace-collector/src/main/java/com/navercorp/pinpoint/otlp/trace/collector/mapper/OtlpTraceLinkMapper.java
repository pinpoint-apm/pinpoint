package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.server.util.Base16Utils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.trace.v1.Span;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils.getAttributeToMap;

@Component
public class OtlpTraceLinkMapper {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectWriter mapWriter;

    public OtlpTraceLinkMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.mapWriter = objectMapper.writerFor(new TypeReference<Map<String, Object>>() {});
    }

    public void addLinkToAnnotation(Span.Link link, AnnotationWriter annotationWriter) {
        // OTel spec requires both trace_id and span_id on a Link; skip malformed entries
        // so downstream consumers never see a Link annotation that points nowhere.
        if (link.getTraceId().isEmpty() || link.getSpanId().isEmpty()) {
            return;
        }

        final String traceId = Base16Utils.encodeToString(link.getTraceId().toByteArray());
        final String spanId = Base16Utils.encodeToString(link.getSpanId().toByteArray());

        try {
            final Map<String, Object> payload = new LinkedHashMap<>(5);
            payload.put("traceId", traceId);
            payload.put("spanId", spanId);
            if (!link.getTraceState().isEmpty()) {
                payload.put("traceState", link.getTraceState());
            }
            if (link.getFlags() != 0) {
                payload.put("flags", link.getFlags());
            }
            payload.put("attributes", getLinkAttributes(link));

            final String value = mapWriter.writeValueAsString(payload);
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), value));
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize otel span link, traceId={}, spanId={}", traceId, spanId, e);
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), "json processing error"));
        }
    }

    private Map<String, Object> getLinkAttributes(Span.Link link) {
        if (link.getAttributesCount() == 0) {
            return Map.of();
        }
        return getAttributeToMap(link.getAttributesList());
    }
}
