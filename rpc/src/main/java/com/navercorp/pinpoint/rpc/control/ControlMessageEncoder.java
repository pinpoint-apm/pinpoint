/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.control;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.common.Charsets;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
   be simple. this is similar to NPC but use "bit" operation instead of Chunk in String.
   permit only utf-8 encoding.

 * @author koo.taejin
 */
public class ControlMessageEncoder {


    private Charset charset;

    public ControlMessageEncoder() {
        this.charset = Charsets.UTF_8;
    }

    public byte[] encode(Map<String, Object> value) throws ProtocolException {
        ChannelBuffer cb = ChannelBuffers.dynamicBuffer(100);
        encode(value, cb);

        int writeIndex = cb.writerIndex();
        byte[] result = new byte[writeIndex];

        cb.readBytes(result);

        return result;
    }

    private void encode(Map<String, Object> value, ChannelBuffer cb) throws ProtocolException {
        encodeMap(value, cb);
    }

    private void encode(Object value, ChannelBuffer cb) throws ProtocolException {
//        try {
            if (value == null) {
                encodeNull(cb);
            } else if (value instanceof String) {
                encodeString((String) value, cb);
            } else if (value instanceof Boolean) {
                encodeBoolean((Boolean) value, cb);
            } else if (value instanceof Short) {
                encodeInt((Short) value, cb);
            } else if (value instanceof Integer) {
                encodeInt((Integer) value, cb);
            } else if (value instanceof Long) {
                encodeLong((Long) value, cb);
            } else if (value instanceof Float) {
                encodeDouble(((Float) value).doubleValue(), cb);
            } else if (value instanceof Double) {
                encodeDouble((Double) value, cb);
            } else if (value instanceof Number) { // Other numbers (i.e.
                // BigInteger and BigDecimal)
                encodeString(value.toString(), cb);
            } else if (value instanceof Collection) {
                encodeCollection((Collection<?>) value, cb);
            } else if (value instanceof Map) {
                encodeMap((Map<?, ?>) value, cb);
            } else if (value.getClass().isArray()) {
                int arraySize = Array.getLength(value);

                List<Object> arrayToList = new ArrayList<Object>(arraySize);
                for (int i = 0; i < arraySize; i++) {
                    arrayToList.add(Array.get(value, i));
                }
                encodeCollection(arrayToList, cb);
            } else {
                throw new ProtocolException("Unsupported type : " + value.getClass().getName());
            }
//        } catch (Exception e) {
//            throw new ProtocolException(e);
//        }
    }

    private void encodeNull(ChannelBuffer out) {
        out.writeByte((byte) ControlMessageProtocolConstant.TYPE_CHARACTER_NULL);
    }

    private void encodeString(String value, ChannelBuffer out) {
        out.writeByte((byte) ControlMessageProtocolConstant.TYPE_CHARACTER_STRING);
        putPrefixedBytes(value.getBytes(charset), out);
    }

    private void encodeBoolean(boolean value, ChannelBuffer out) {
        if (value) {
            out.writeByte((byte) ControlMessageProtocolConstant.TYPE_CHARACTER_BOOL_TRUE);
        } else {
            out.writeByte((byte) ControlMessageProtocolConstant.TYPE_CHARACTER_BOOL_FALSE);
        }
    }

    private void encodeInt(int value, ChannelBuffer out) {
        out.writeByte((byte) ControlMessageProtocolConstant.TYPE_CHARACTER_INT);

        out.writeByte((byte) (value >> 24));
        out.writeByte((byte) (value >> 16));
        out.writeByte((byte) (value >> 8));
        out.writeByte((byte) (value));
    }

    private void encodeLong(long value, ChannelBuffer out) {
        out.writeByte((byte) ControlMessageProtocolConstant.TYPE_CHARACTER_LONG);

        out.writeByte((byte) (value >> 56));
        out.writeByte((byte) (value >> 48));
        out.writeByte((byte) (value >> 40));
        out.writeByte((byte) (value >> 32));
        out.writeByte((byte) (value >> 24));
        out.writeByte((byte) (value >> 16));
        out.writeByte((byte) (value >> 8));
        out.writeByte((byte) (value));
    }

    private void encodeDouble(double value, ChannelBuffer out) {
        out.writeByte((byte) ControlMessageProtocolConstant.TYPE_CHARACTER_DOUBLE);

        long longValue = Double.doubleToLongBits(value);

        out.writeByte((byte) (longValue >> 56));
        out.writeByte((byte) (longValue >> 48));
        out.writeByte((byte) (longValue >> 40));
        out.writeByte((byte) (longValue >> 32));
        out.writeByte((byte) (longValue >> 24));
        out.writeByte((byte) (longValue >> 16));
        out.writeByte((byte) (longValue >> 8));
        out.writeByte((byte) (longValue));
    }

    private void encodeCollection(Collection<?> collection, ChannelBuffer out) throws ProtocolException {
        out.writeByte((byte) ControlMessageProtocolConstant.CONTROL_CHARACTER_LIST_START);
        for (Object element : collection) {
            encode(element, out);
        }
        out.writeByte((byte) ControlMessageProtocolConstant.CONTROL_CHARACTER_LIST_END);
    }

    private void encodeMap(Map<?, ?> map, ChannelBuffer out) throws ProtocolException {
        out.writeByte((byte) ControlMessageProtocolConstant.CONTROL_CHARACTER_MAP_START);
        for (Object element : map.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) element;
            encode(entry.getKey(), out);
            encode(entry.getValue(), out);
        }
        out.writeByte((byte) ControlMessageProtocolConstant.CONTROL_CHARACTER_MAP_END);
    }

    private void putPrefixedBytes(byte[] value, ChannelBuffer out) {
        int length = value.length;

        byte[] lengthBuf = new byte[5];

        int idx = 0;
        while (true) {
            if ((length & 0xFFFFFF80) == 0) {
                lengthBuf[(idx++)] = (byte) length;
                break;
            }

            lengthBuf[(idx++)] = (byte) (length & 0x7F | 0x80);

            length >>>= 7;
        }

        for (int i = 0; i < idx; i++) {
            out.writeByte(lengthBuf[i]);
        }

        out.writeBytes(value);
    }

}
