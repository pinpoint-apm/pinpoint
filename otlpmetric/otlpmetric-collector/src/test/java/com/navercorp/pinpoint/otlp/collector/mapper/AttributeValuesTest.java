/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.otlp.collector.mapper;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.common.v1.KeyValueList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeValuesTest {

    @Test
    void toString_scalars() {
        assertEquals("s", AttributeValues.toString(AnyValue.newBuilder().setStringValue("s").build()));
        assertEquals("1234", AttributeValues.toString(AnyValue.newBuilder().setIntValue(1234L).build()));
        assertEquals("1.5", AttributeValues.toString(AnyValue.newBuilder().setDoubleValue(1.5).build()));
        assertEquals("true", AttributeValues.toString(AnyValue.newBuilder().setBoolValue(true).build()));
    }

    @Test
    void toString_kvlist() {
        KeyValue entry = KeyValue.newBuilder().setKey("k").setValue(AnyValue.newBuilder().setStringValue("v")).build();
        AnyValue kvlist = AnyValue.newBuilder().setKvlistValue(KeyValueList.newBuilder().addValues(entry)).build();

        assertEquals(String.valueOf(kvlist.getKvlistValue()), AttributeValues.toString(kvlist));
    }

    @Test
    void toString_unsupported() {
        AnyValue array = AnyValue.newBuilder().setArrayValue(ArrayValue.newBuilder()).build();

        assertEquals(AttributeValues.UNSUPPORTED_VALUE, AttributeValues.toString(array));
        assertEquals(AttributeValues.UNSUPPORTED_VALUE, AttributeValues.toString(AnyValue.getDefaultInstance()));
    }

    @Test
    void toMap() {
        List<KeyValue> attributes = List.of(
                KeyValue.newBuilder().setKey("a").setValue(AnyValue.newBuilder().setStringValue("1")).build(),
                KeyValue.newBuilder().setKey("b").setValue(AnyValue.newBuilder().setIntValue(2L)).build());

        assertEquals(Map.of("a", "1", "b", "2"), AttributeValues.toMap(attributes));
    }
}
