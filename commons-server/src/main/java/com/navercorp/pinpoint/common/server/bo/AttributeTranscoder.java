/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class AttributeTranscoder {

    public static final byte TYPE_STRING = 0;
    public static final byte TYPE_BOOL_TRUE = 1;
    public static final byte TYPE_BOOL_FALSE = 2;
    public static final byte TYPE_LONG = 3;
    public static final byte TYPE_DOUBLE = 4;
    public static final byte TYPE_BYTES = 5;
    public static final byte TYPE_ARRAY = 6;
    public static final byte TYPE_KVLIST = 7;

    public void writeAttributeList(Buffer buffer, List<AttributeBo> attributeBoList) {
        buffer.putVInt(attributeBoList.size());
        for (AttributeBo attributeBo : attributeBoList) {
            buffer.putNullTerminatedString(attributeBo.getKey());
            writeValue(buffer, attributeBo.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void writeValue(Buffer buffer, AttributeValue attributeValue) {
        switch (attributeValue.getType()) {
            case STRING:
                buffer.putByte(TYPE_STRING);
                buffer.putPrefixedBytes(((String) attributeValue.getValue()).getBytes(StandardCharsets.UTF_8));
                break;
            case BOOLEAN:
                buffer.putByte((Boolean) attributeValue.getValue() ? TYPE_BOOL_TRUE : TYPE_BOOL_FALSE);
                break;
            case LONG:
                buffer.putByte(TYPE_LONG);
                buffer.putSVLong((Long) attributeValue.getValue());
                break;
            case DOUBLE:
                buffer.putByte(TYPE_DOUBLE);
                buffer.putLong(Double.doubleToRawLongBits((Double) attributeValue.getValue()));
                break;
            case BYTES:
                buffer.putByte(TYPE_BYTES);
                buffer.putPrefixedBytes((byte[]) attributeValue.getValue());
                break;
            case ARRAY:
                buffer.putByte(TYPE_ARRAY);
                List<AttributeValue> array = (List<AttributeValue>) attributeValue.getValue();
                buffer.putVInt(array.size());
                for (AttributeValue item : array) {
                    writeValue(buffer, item);
                }
                break;
            case KEY_VALUE_LIST:
                buffer.putByte(TYPE_KVLIST);
                List<AttributeKeyValue> kvList = (List<AttributeKeyValue>) attributeValue.getValue();
                buffer.putVInt(kvList.size());
                for (AttributeKeyValue kv : kvList) {
                    buffer.putNullTerminatedString(kv.getKey());
                    writeValue(buffer, kv.getValue());
                }
                break;
        }
    }

    public List<AttributeBo> readAttributeList(Buffer buffer) {
        final int size = buffer.readVInt();
        final List<AttributeBo> attributeBoList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final String key = buffer.readNullTerminatedString();
            final AttributeValue value = readValue(buffer);
            attributeBoList.add(new AttributeBo(key, value));
        }
        return attributeBoList;
    }

    private AttributeValue readValue(Buffer buffer) {
        final byte typeCode = buffer.readByte();
        switch (typeCode) {
            case TYPE_STRING:
                byte[] strBytes = buffer.readPrefixedBytes();
                return AttributeValue.of(new String(strBytes, StandardCharsets.UTF_8));
            case TYPE_BOOL_TRUE:
                return AttributeValue.of(true);
            case TYPE_BOOL_FALSE:
                return AttributeValue.of(false);
            case TYPE_LONG:
                return AttributeValue.of(buffer.readSVLong());
            case TYPE_DOUBLE:
                return AttributeValue.of(Double.longBitsToDouble(buffer.readLong()));
            case TYPE_BYTES:
                return AttributeValue.of(buffer.readPrefixedBytes());
            case TYPE_ARRAY: {
                int arraySize = buffer.readVInt();
                List<AttributeValue> list = new ArrayList<>(arraySize);
                for (int i = 0; i < arraySize; i++) {
                    list.add(readValue(buffer));
                }
                return AttributeValue.of(list);
            }
            case TYPE_KVLIST: {
                int kvSize = buffer.readVInt();
                AttributeKeyValue[] kvArray = new AttributeKeyValue[kvSize];
                for (int i = 0; i < kvSize; i++) {
                    String key = buffer.readNullTerminatedString();
                    AttributeValue val = readValue(buffer);
                    kvArray[i] = AttributeKeyValue.of(key, val);
                }
                return AttributeValue.ofAttributeKeyValueList(kvArray);
            }
            default:
                throw new IllegalStateException("Unknown attribute type code: " + typeCode);
        }
    }
}
