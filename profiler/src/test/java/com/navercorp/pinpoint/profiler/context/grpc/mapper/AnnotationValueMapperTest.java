package com.navercorp.pinpoint.profiler.context.grpc.mapper;


import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.util.BytesStringStringValue;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PBytesStringStringValue;
import com.navercorp.pinpoint.grpc.trace.PIntBooleanIntBooleanValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringStringValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringValue;
import com.navercorp.pinpoint.grpc.trace.PLongIntIntByteByteStringValue;
import com.navercorp.pinpoint.grpc.trace.PStringStringValue;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Woonduk Kang(emeroad)
 */
class AnnotationValueMapperTest {
    private final AnnotationValueMapper mapper = new AnnotationValueMapperImpl();


    @Test
    public void buildPAnnotationValue_null() {
        PAnnotationValue value = mapper.map(Annotations.of(1));
        assertNull(value);
    }

    @Test
    public void buildPAnnotationValue_primitive() {
        PAnnotationValue value = mapper.map(Annotations.of(1, "foo"));
        assertEquals("foo", value.getStringValue());

        value = mapper.map(Annotations.of(1, Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, value.getIntValue());

        value = mapper.map(Annotations.of(1, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, value.getLongValue());

        value = mapper.map(Annotations.of(1, Boolean.TRUE));
        assertEquals(Boolean.TRUE, value.getBoolValue());

        value = mapper.map(Annotations.of(1, Byte.MAX_VALUE));
        assertEquals(Byte.MAX_VALUE, value.getByteValue());

        value = mapper.map(Annotations.of(1, Float.MAX_VALUE));
        assertEquals(Float.MAX_VALUE, value.getDoubleValue(), 0);

        value = mapper.map(Annotations.of(1, Double.MAX_VALUE));
        assertEquals(Double.MAX_VALUE, value.getDoubleValue(), 0);

        value = mapper.map(Annotations.of(1, "foo".getBytes()));
        assertEquals(ByteString.copyFrom("foo".getBytes()), value.getBinaryValue());

        value = mapper.map(Annotations.of(1, Short.MAX_VALUE));
        assertEquals(Short.MAX_VALUE, value.getShortValue());
    }

    static class User {
        private final String name;

        public User(String name) {
            this.name = Objects.requireNonNull(name, "name");
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Test
    public void buildPAnnotationValue_Object() {
        PAnnotationValue value = mapper.map(Annotations.of(1, new AnnotationValueMapperTest.User("name")));
        assertEquals("name", value.getStringValue());
    }

    @Test
    public void buildPAnnotationValue_IntString() {
        IntStringValue intStringValue = new IntStringValue(1, "2");

        PAnnotationValue container = mapper.map(Annotations.of(1, intStringValue));
        PIntStringValue pAnnotation = container.getIntStringValue();

        Assertions.assertEquals(1, pAnnotation.getIntValue());
        Assertions.assertEquals("2", pAnnotation.getStringValue().getValue());
    }


    @Test
    public void buildPAnnotationValue_StringString() {
        StringStringValue intStringValue = new StringStringValue("1", "2");

        PAnnotationValue container = mapper.map(Annotations.of(1, intStringValue));
        PStringStringValue pAnnotation = container.getStringStringValue();

        Assertions.assertEquals("1", pAnnotation.getStringValue1().getValue());
        Assertions.assertEquals("2", pAnnotation.getStringValue2().getValue());
    }


    @Test
    public void buildPAnnotationValue_IntStringStringValue() {
        IntStringStringValue intStringValue = new IntStringStringValue(1, "2", "3");

        PAnnotationValue container = mapper.map(Annotations.of(1, intStringValue));
        PIntStringStringValue pAnnotation = container.getIntStringStringValue();

        Assertions.assertEquals(1, pAnnotation.getIntValue());
        Assertions.assertEquals("2", pAnnotation.getStringValue1().getValue());
        Assertions.assertEquals("3", pAnnotation.getStringValue2().getValue());
    }

    @Test
    public void buildPAnnotationValue_BytesStringStringValue() {
        BytesStringStringValue bytesStringStringValue = new BytesStringStringValue("test".getBytes(), "1", "2");

        PAnnotationValue container = mapper.map(Annotations.of(1, bytesStringStringValue));
        PBytesStringStringValue pAnnotation = container.getBytesStringStringValue();

        Assertions.assertEquals(ByteString.copyFrom("test".getBytes()), pAnnotation.getBytesValue());
        Assertions.assertEquals("1", pAnnotation.getStringValue1().getValue());
        Assertions.assertEquals("2", pAnnotation.getStringValue2().getValue());
    }

    @Test
    public void buildPAnnotationValue_LongIntIntByteByteStringValue() {
        LongIntIntByteByteStringValue intStringValue = new LongIntIntByteByteStringValue(
                1L, 2, 3, (byte) 4, (byte) 5, "6");

        PAnnotationValue container = mapper.map(Annotations.of(1, intStringValue));
        PLongIntIntByteByteStringValue pAnnotation = container.getLongIntIntByteByteStringValue();

        Assertions.assertEquals(1, pAnnotation.getLongValue());
        Assertions.assertEquals(2, pAnnotation.getIntValue1());
        Assertions.assertEquals(3, pAnnotation.getIntValue2());
        Assertions.assertEquals(4, pAnnotation.getByteValue1());
        Assertions.assertEquals(5, pAnnotation.getByteValue2());
        Assertions.assertEquals("6", pAnnotation.getStringValue().getValue());
    }

    @Test
    public void buildPAnnotationValue_IntBooleanIntBooleanValue() {
        IntBooleanIntBooleanValue intStringValue = new IntBooleanIntBooleanValue(
                1, true, 3, false);

        PAnnotationValue container = mapper.map(Annotations.of(1, intStringValue));
        PIntBooleanIntBooleanValue pAnnotation = container.getIntBooleanIntBooleanValue();

        Assertions.assertEquals(1, pAnnotation.getIntValue1());
        Assertions.assertEquals(true, pAnnotation.getBoolValue1());
        Assertions.assertEquals(3, pAnnotation.getIntValue2());
        Assertions.assertEquals(false, pAnnotation.getBoolValue2());
    }
    
}