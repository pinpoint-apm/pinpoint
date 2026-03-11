package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils.getAttributeToMap;

public class OtlpTraceAttributeMapper {

    private final ObjectWriter mapWriter;

    public OtlpTraceAttributeMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.mapWriter = objectMapper.writerFor(new TypeReference<Map<String, Object>>() {});
    }

    public void addAttributesToAnnotation(List<KeyValue> keyValueList, AnnotationWriter annotationWriter) {
        try {
            Map<String, Object> map = getAttributeToMap(keyValueList, OtlpTraceConstants.FILTERED_ATTRIBUTE_KEY);
            if (!map.isEmpty()) {
                final String value = mapWriter.writeValueAsString(map);
                annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), value));
            }
        } catch (JsonProcessingException e) {
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_ATTRIBUTE.getCode(), "json processing error"));
        }
    }

}
