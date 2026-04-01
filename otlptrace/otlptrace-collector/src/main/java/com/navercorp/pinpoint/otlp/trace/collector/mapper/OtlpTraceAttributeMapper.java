package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.trace.AnnotationKey;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class OtlpTraceAttributeMapper {

    private final ObjectWriter mapWriter;

    public OtlpTraceAttributeMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.mapWriter = objectMapper.writerFor(new TypeReference<Map<String, Object>>() {});
    }

    public void addAttributesToAnnotation(Map<String, Object> attributes, AnnotationWriter annotationWriter) {
        try {
            Map<String, Object> map = new HashMap<>(attributes.size());
            for(Map.Entry<String, Object> entry : attributes.entrySet()) {
                if(!OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY.test(entry.getKey())) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            if (!map.isEmpty()) {
                final String value = mapWriter.writeValueAsString(map);
                annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), value));
            }
        } catch (JsonProcessingException e) {
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), "json processing error"));
        }
    }
}
