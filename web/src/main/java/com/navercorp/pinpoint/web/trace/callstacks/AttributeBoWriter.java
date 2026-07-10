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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValueList;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueArray;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueBoolean;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueBytes;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueDouble;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueLong;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueString;
import org.apache.commons.io.output.StringBuilderWriter;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * Writes a list of {@link AttributeBo} to a JSON string by streaming the typed
 * {@link AttributeValue} tree directly through a {@link JsonGenerator}, without building
 * an intermediate {@code Map}.
 */
public class AttributeBoWriter {

    static final String JSON_PROCESSING_ERROR = "json processing error";

    private final ObjectMapper mapper;

    public AttributeBoWriter(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public String toJson(List<AttributeBo> attributeBoList) {
        StringBuilderWriter writer = new StringBuilderWriter();
        try (JsonGenerator gen = mapper.createGenerator(writer)) {
            gen.writeStartObject();
            for (AttributeBo attr : attributeBoList) {
                gen.writeFieldName(attr.getKey());
                writeValue(gen, attr.getValue());
            }
            gen.writeEndObject();
        } catch (IOException e) {
            return JSON_PROCESSING_ERROR;
        }
        return writer.toString();
    }

    private static void writeValue(JsonGenerator gen, AttributeValue value) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        switch (value.getType()) {
            case STRING -> gen.writeString(((AttributeValueString) value).getStringValue());
            case BOOLEAN -> gen.writeBoolean(((AttributeValueBoolean) value).getBooleanValue());
            case LONG -> gen.writeNumber(((AttributeValueLong) value).getLongValue());
            case DOUBLE -> gen.writeNumber(((AttributeValueDouble) value).getDoubleValue());
            case BYTES -> {
                byte[] bytesValue = ((AttributeValueBytes) value).getRawBytesValue();
                gen.writeString(Base64.getEncoder().encodeToString(bytesValue));
            }
            case ARRAY -> {
                gen.writeStartArray();
                for (AttributeValue item : ((AttributeValueArray) value).getArrayValue()) {
                    writeValue(gen, item);
                }
                gen.writeEndArray();
            }
            case KEY_VALUE_LIST -> {
                gen.writeStartObject();
                for (AttributeKeyValue kv : ((AttributeKeyValueList) value).getKeyValueListValue()) {
                    gen.writeFieldName(kv.getKey());
                    writeValue(gen, kv.getValue());
                }
                gen.writeEndObject();
            }
        }
    }
}
