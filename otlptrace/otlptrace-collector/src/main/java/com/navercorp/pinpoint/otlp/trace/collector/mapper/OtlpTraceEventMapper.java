package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.io.AnnotationWriter;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import io.opentelemetry.proto.trace.v1.Span;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants.ATTRIBUTE_KEY_EXCEPTION_STACKTRACE;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants.EVENT_NAME_EXCEPTION;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils.getAttributeToMap;

@Component
public class OtlpTraceEventMapper {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectWriter mapWriter;

    public OtlpTraceEventMapper(ObjectMapper objectMapper) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.mapWriter = objectMapper.writerFor(new TypeReference<Map<String, Object>>() {});
    }

    public void addEventToAnnotation(Span.Event event, AnnotationWriter annotationWriter) {
        final String name = event.getName();
        if (StringUtils.isEmpty(name)) {
            return;
        }

        try {
            final Map<String, Object> payload = new LinkedHashMap<>(3);
            payload.put("name", name);
            if (event.getTimeUnixNano() > 0) {
                payload.put("timeUnixNano", event.getTimeUnixNano());
            }
            payload.put("attributes", getEventAttributes(event));

            final String value = mapWriter.writeValueAsString(payload);
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_EVENT.getCode(), value));
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize otel span event, name={}", name, e);
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_EVENT.getCode(), "json processing error"));
        }
    }

    private Map<String, Object> getEventAttributes(Span.Event event) {
        if (event.getAttributesCount() == 0) {
            return Map.of();
        }
        if (EVENT_NAME_EXCEPTION.equals(event.getName())) {
            // exception.stacktrace is already preserved as ExceptionMetaDataBo via OtlpExceptionMapper.
            // Keeping it here would duplicate a potentially large payload on every span annotation.
            return getAttributeToMap(event.getAttributesList(), ATTRIBUTE_KEY_EXCEPTION_STACKTRACE::equals);
        }
        return getAttributeToMap(event.getAttributesList());
    }

}
