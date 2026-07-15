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
package com.navercorp.pinpoint.web.trace.callstacks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeBoWriterTest {

    private final AttributeBoWriter writer = new AttributeBoWriter(new ObjectMapper());

    @Test
    void toJson_writesScalarTypes() {
        List<AttributeBo> attributes = List.of(
                new AttributeBo("string", AttributeValue.of("value")),
                new AttributeBo("boolean", AttributeValue.of(true)),
                new AttributeBo("long", AttributeValue.of(127L)),
                new AttributeBo("double", AttributeValue.of(0.5d)));

        assertEquals("""
                        {"string":"value","boolean":true,"long":127,"double":0.5}""",
                writer.toJson(attributes));
    }

    @Test
    void toJson_writesBytesAsBase64() {
        // 0xF8/0xFF... produce '+' and '/' — exercises alphabet equality between
        // writeBinary's MIME_NO_LINEFEEDS variant and Base64.getEncoder()
        byte[] bytes = {(byte) 0xF8, 1, 2, (byte) 0xFF, (byte) 0xFE, (byte) 0xFD, 4};
        List<AttributeBo> attributes = List.of(new AttributeBo("bytes", AttributeValue.of(bytes)));

        String expected = String.format("""
                {"bytes":"%s"}""", Base64.getEncoder().encodeToString(bytes));
        assertEquals(expected, writer.toJson(attributes));
    }

    @Test
    void toJson_writesNestedArrayAndKeyValueList() {
        AttributeValue array = AttributeValue.of(AttributeValue.of("a"), AttributeValue.of(1L));
        AttributeValue keyValueList = AttributeValue.ofAttributeKeyValueList(
                AttributeKeyValue.of("inner", AttributeValue.of(true)));
        List<AttributeBo> attributes = List.of(
                new AttributeBo("array", array),
                new AttributeBo("kv", keyValueList));

        assertEquals("""
                {"array":["a",1],"kv":{"inner":true}}""", writer.toJson(attributes));
    }

    @Test
    void toJson_emptyAttributes() {
        assertEquals("{}", writer.toJson(List.of()));
    }
}
