package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.server.util.Base16Utils;
import com.navercorp.pinpoint.common.server.util.ByteStringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.trace.v1.Span;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils.getAttributeToMap;

@Component
public class OtlpTraceLinkMapper {

    private final ObjectWriter mapWriter;

    public OtlpTraceLinkMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.mapWriter = objectMapper.writerFor(new TypeReference<Map<String, Object>>() {});
    }

    public void addLinkToAnnotation(Span.Link link, AnnotationWriter annotationWriter) {
        try {
            Map<String, Object> map = new HashMap<>();
            if (!link.getTraceId().isEmpty()) {
                map.put("traceId", Base16Utils.encodeToString(link.getTraceId().toByteArray()));
            }
            if (!link.getSpanId().isEmpty()) {
                // Stored as decimal String to avoid JS Number precision loss for any consumer that
                // reads the raw annotation JSON directly. Java readers (OtelLinkValue.readLong)
                // accept both number and decimal-string forms, preserving backward compatibility.
                map.put("spanId", String.valueOf(ByteStringUtils.parseLong(link.getSpanId())));
            }
            if (link.getAttributesCount() > 0) {
                map.put("attributes", getAttributeToMap(link.getAttributesList()));
            }
            final String value = mapWriter.writeValueAsString(map);
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), value));
        } catch (JsonProcessingException e) {
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(), "json processing error"));
        }
    }
}
