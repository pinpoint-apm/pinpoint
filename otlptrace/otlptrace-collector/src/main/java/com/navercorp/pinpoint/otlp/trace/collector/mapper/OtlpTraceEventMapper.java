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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils.getAttributeToMap;

@Component
public class OtlpTraceEventMapper {

    private static final String FIELD_TIME = "time";
    private static final String FIELD_ATTRIBUTES = "attributes";

    private final ObjectWriter mapWriter;
    private final int valueMaxBytes;

    public OtlpTraceEventMapper(ObjectMapper objectMapper,
                                @Value("${pinpoint.collector.otlptrace.event.value-max-bytes:8192}") int valueMaxBytes) {
        Objects.requireNonNull(objectMapper, "objectMapper");
        this.mapWriter = objectMapper.writerFor(new TypeReference<Map<String, Object>>() {});
        this.valueMaxBytes = valueMaxBytes;
    }

    /**
     * Serializes an OTel {@link Span.Event} as JSON wrapped in the parent event name:
     * <pre>{@code {"<event.name>": {"time": <unix_nano>, "attributes": {...}}}}</pre>
     * The {@code attributes} field is omitted when the event has none, so empty events
     * produce {@code {"<event.name>": {"time": N}}} (no longer a heterogeneous string/object
     * value). Emitted under {@link AnnotationKey#OPENTELEMETRY_EVENT}.
     */
    public int addEventToAnnotation(Span.Event event, AnnotationWriter annotationWriter) {
        if (StringUtils.isEmpty(event.getName())) {
            return 0;
        }

        int truncated = 0;
        try {
            Map<String, Object> inner = new HashMap<>(2);
            inner.put(FIELD_TIME, event.getTimeUnixNano());
            if (event.getAttributesCount() > 0) {
                // Cap over-long string values (notably exception.stacktrace, which is also kept in
                // full in exception metadata) so a single event cannot bloat the span row.
                final TransformContext context = new TransformContext(valueMaxBytes);
                final Map<String, Object> eventAttributes =
                        getAttributeToMap(event.getAttributesList(), context);
                truncated = context.truncatedCount();
                inner.put(FIELD_ATTRIBUTES, eventAttributes);
            }
            Map<String, Object> map = Map.of(event.getName(), inner);
            final String value = mapWriter.writeValueAsString(map);
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_EVENT.getCode(), value));
        } catch (JsonProcessingException e) {
            annotationWriter.write(AnnotationBo.of(AnnotationKey.OPENTELEMETRY_EVENT.getCode(), "json processing error"));
        }
        return truncated;
    }

}
