package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import io.opentelemetry.proto.trace.v1.Span;
import org.apache.commons.io.output.StringBuilderWriter;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OtlpTraceEventMapper {

    private static final String FIELD_TIME = "time";
    private static final String FIELD_ATTRIBUTES = "attributes";

    private final ObjectMapper objectMapper;
    private final int valueMaxBytes;

    public OtlpTraceEventMapper(ObjectMapper objectMapper,
                                @Value("${pinpoint.collector.otlptrace.event.value-max-bytes:8192}") int valueMaxBytes) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        if (valueMaxBytes < 0) {
            throw new IllegalArgumentException("valueMaxBytes must be >= 0: " + valueMaxBytes);
        }
        this.valueMaxBytes = valueMaxBytes;
    }

    /**
     * Serializes an OTel {@link Span.Event} as {@link OtelEvent} JSON into an
     * {@link AnnotationKey#OPENTELEMETRY_EVENT} annotation.
     *
     * <p>Returns {@code null} when the event name is empty — such an event is invalid input
     * we drop silently.</p>
     *
     * <p>{@code onTruncated} is invoked once per truncated attribute leaf.</p>
     */
    public @Nullable AnnotationBo toAnnotation(Span.Event event, TruncationListener onTruncated) {
        if (StringUtils.isEmpty(event.getName())) {
            return null;
        }
        try {
            final OtelEvent otelEvent = toOtelEvent(event);
            final String value = toJson(otelEvent, onTruncated);
            return AnnotationBo.of(AnnotationKey.OPENTELEMETRY_EVENT.getCode(), value);
        } catch (JacksonException e) {
            return AnnotationBo.of(AnnotationKey.OPENTELEMETRY_EVENT.getCode(), "json processing error");
        }
    }

    private OtelEvent toOtelEvent(Span.Event event) {
        return new OtelEvent(event.getName(), event.getTimeUnixNano(), event.getAttributesList());
    }

    private String toJson(OtelEvent otelEvent, TruncationListener onTruncated) {
        final StringBuilderWriter buffer = new StringBuilderWriter();
        try (JsonGenerator generator = objectMapper.createGenerator(buffer)) {
            generator.writeStartObject();
            generator.writeName(otelEvent.name());

            generator.writeStartObject();
            generator.writeNumberProperty(FIELD_TIME, otelEvent.time());
            if (!otelEvent.attributes().isEmpty()) {
                // Cap over-long string values (notably exception.stacktrace, which is also kept in
                // full in exception metadata) so a single event cannot bloat the span row.
                generator.writeName(FIELD_ATTRIBUTES);
                OtlpTraceMapperUtils.writeAttributes(generator, otelEvent.attributes(), valueMaxBytes, onTruncated);
            }
            generator.writeEndObject();

            generator.writeEndObject();
        }
        return buffer.toString();
    }

}
