/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PIntBooleanIntBooleanValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringStringValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringValue;
import com.navercorp.pinpoint.grpc.trace.PLongIntIntByteByteStringValue;
import com.navercorp.pinpoint.grpc.trace.PStringStringValue;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcAnnotationValueMapperTest {

    private final GrpcAnnotationValueMapper mapper = new GrpcAnnotationValueMapper();

    @Test
    public void buildPAnnotationValue_null() {
        PAnnotationValue value = mapper.buildPAnnotationValue(Annotations.of(1));
        assertNull(value);
    }

    @Test
    public void buildPAnnotationValue_primitive() {
        PAnnotationValue value = mapper.buildPAnnotationValue(Annotations.of(1, "foo"));
        assertEquals("foo", value.getStringValue());

        value = mapper.buildPAnnotationValue(Annotations.of(1, Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, value.getIntValue());

        value = mapper.buildPAnnotationValue(Annotations.of(1, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, value.getLongValue());

        value = mapper.buildPAnnotationValue(Annotations.of(1, Boolean.TRUE));
        assertEquals(Boolean.TRUE, value.getBoolValue());

        value = mapper.buildPAnnotationValue(Annotations.of(1, Byte.MAX_VALUE));
        assertEquals(Byte.MAX_VALUE, value.getByteValue());

        value = mapper.buildPAnnotationValue(Annotations.of(1, Float.MAX_VALUE));
        assertEquals(Float.MAX_VALUE, value.getDoubleValue(), 0);

        value = mapper.buildPAnnotationValue(Annotations.of(1, Double.MAX_VALUE));
        assertEquals(Double.MAX_VALUE, value.getDoubleValue(), 0);

        value = mapper.buildPAnnotationValue(Annotations.of(1, "foo".getBytes()));
        assertEquals(ByteString.copyFrom("foo".getBytes()), value.getBinaryValue());

        value = mapper.buildPAnnotationValue(Annotations.of(1, Short.MAX_VALUE));
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
        PAnnotationValue value = mapper.buildPAnnotationValue(Annotations.of(1, new User("name")));
        assertEquals("name", value.getStringValue());
    }

    @Test
    public void buildPAnnotationValue_IntString() {
        IntStringValue intStringValue = new IntStringValue(1, "2");

        PAnnotationValue container = mapper.buildPAnnotationValue(Annotations.of(1, intStringValue));
        PIntStringValue pAnnotation = container.getIntStringValue();

        Assert.assertEquals(pAnnotation.getIntValue(), 1);
        Assert.assertEquals(pAnnotation.getStringValue().getValue(), "2");
    }


    @Test
    public void buildPAnnotationValue_StringString() {
        StringStringValue intStringValue = new StringStringValue("1", "2");

        PAnnotationValue container = mapper.buildPAnnotationValue(Annotations.of(1, intStringValue));
        PStringStringValue pAnnotation = container.getStringStringValue();

        Assert.assertEquals(pAnnotation.getStringValue1().getValue(), "1");
        Assert.assertEquals(pAnnotation.getStringValue2().getValue(), "2");
    }


    @Test
    public void buildPAnnotationValue_IntStringStringValue() {
        IntStringStringValue intStringValue = new IntStringStringValue(1, "2", "3");

        PAnnotationValue container = mapper.buildPAnnotationValue(Annotations.of(1, intStringValue));
        PIntStringStringValue pAnnotation = container.getIntStringStringValue();

        Assert.assertEquals(pAnnotation.getIntValue(), 1);
        Assert.assertEquals(pAnnotation.getStringValue1().getValue(), "2");
        Assert.assertEquals(pAnnotation.getStringValue2().getValue(), "3");
    }

    @Test
    public void buildPAnnotationValue_LongIntIntByteByteStringValue() {
        LongIntIntByteByteStringValue intStringValue = new LongIntIntByteByteStringValue(
                1L, 2, 3, (byte) 4, (byte) 5, "6");

        PAnnotationValue container = mapper.buildPAnnotationValue(Annotations.of(1, intStringValue));
        PLongIntIntByteByteStringValue pAnnotation = container.getLongIntIntByteByteStringValue();

        Assert.assertEquals(pAnnotation.getLongValue(), 1);
        Assert.assertEquals(pAnnotation.getIntValue1(), 2);
        Assert.assertEquals(pAnnotation.getIntValue2(), 3);
        Assert.assertEquals(pAnnotation.getByteValue1(), 4);
        Assert.assertEquals(pAnnotation.getByteValue2(), 5);
        Assert.assertEquals(pAnnotation.getStringValue().getValue(), "6");
    }

    @Test
    public void buildPAnnotationValue_IntBooleanIntBooleanValue() {
        IntBooleanIntBooleanValue intStringValue = new IntBooleanIntBooleanValue(
                1, true, 3, false);

        PAnnotationValue container = mapper.buildPAnnotationValue(Annotations.of(1, intStringValue));
        PIntBooleanIntBooleanValue pAnnotation = container.getIntBooleanIntBooleanValue();

        Assert.assertEquals(pAnnotation.getIntValue1(), 1);
        Assert.assertEquals(pAnnotation.getBoolValue1(), true);
        Assert.assertEquals(pAnnotation.getIntValue2(), 3);
        Assert.assertEquals(pAnnotation.getBoolValue2(), false);
    }
}