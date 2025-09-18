package com.navercorp.pinpoint.profiler.context.grpc.mapper;

import com.navercorp.pinpoint.grpc.trace.PAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanAutoUriGetter;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanUriGetter;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SpanMessageMapperTest {

    @Test
    void map() {
        AnnotationValueMapper annotationValueMapper = Mappers.getMapper(AnnotationValueMapper.class);
        SpanUriGetter spanUriGetter = new SpanAutoUriGetter();
        SpanMessageMapper spanMessageMapper = new SpanMessageMapperImpl(annotationValueMapper, spanUriGetter);

        PAnnotation pAnnotation = spanMessageMapper.map(Annotations.of(1, "foo"));
        assertEquals(1, pAnnotation.getKey());
        assertEquals("foo", pAnnotation.getValue().getStringValue());


        Object nullObject = null;
        pAnnotation = spanMessageMapper.map(Annotations.of(99, nullObject));
        assertEquals(99, pAnnotation.getKey());
        assertFalse(pAnnotation.hasValue());
    }
}