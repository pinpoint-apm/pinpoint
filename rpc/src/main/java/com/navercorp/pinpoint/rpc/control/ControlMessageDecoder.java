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

import com.navercorp.pinpoint.common.Charsets;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author koo.taejin
 */
public class ControlMessageDecoder {

    private Charset charset;

    public ControlMessageDecoder() {
        this.charset = Charsets.UTF_8;
    }

    public Object decode(byte[] in) throws ProtocolException {
        return decode(ByteBuffer.wrap(in));
    }

    public Object decode(ByteBuffer in) throws ProtocolException {
        byte type = in.get();
        switch (type) {
        case ControlMessageProtocolConstant.TYPE_CHARACTER_NULL:
            return null;
        case ControlMessageProtocolConstant.TYPE_CHARACTER_BOOL_TRUE:
            return Boolean.TRUE;
        case ControlMessageProtocolConstant.TYPE_CHARACTER_BOOL_FALSE:
            return Boolean.FALSE;
        case ControlMessageProtocolConstant.TYPE_CHARACTER_INT:
            return in.getInt();
        case ControlMessageProtocolConstant.TYPE_CHARACTER_LONG:
            return in.getLong();
        case ControlMessageProtocolConstant.TYPE_CHARACTER_DOUBLE:
            return Double.longBitsToDouble(in.getLong());
        case ControlMessageProtocolConstant.TYPE_CHARACTER_STRING:
            return decodeString(in);
        case ControlMessageProtocolConstant.CONTROL_CHARACTER_LIST_START:
            List<Object> answerList = new ArrayList<Object>();
            while (!isListFinished(in)) {
                answerList.add(decode(in));
            }
            in.get(); // Skip the terminator
            return answerList;
        case ControlMessageProtocolConstant.CONTROL_CHARACTER_MAP_START:
            Map<Object, Object> answerMap = new LinkedHashMap<Object, Object>();
            while (!isMapFinished(in)) {
                Object key = decode(in);
                Object value = decode(in);
                answerMap.put(key, value);
            }
            in.get(); // Skip the terminator
            return answerMap;
        default:
            throw new ProtocolException("invalid type character: " + (char) type + " (" + "0x" + Integer.toHexString(type) + ")");
        }
    }

    private Object decodeString(ByteBuffer in) {
        int length = readStringLength(in);

        byte[] bytesToEncode = new byte[length];
        in.get(bytesToEncode);

        return new String(bytesToEncode, charset);
    }

    private boolean isMapFinished(ByteBuffer in) {
        return in.get(in.position()) == ControlMessageProtocolConstant.CONTROL_CHARACTER_MAP_END;
    }

    private boolean isListFinished(ByteBuffer in) {
        return in.get(in.position()) == ControlMessageProtocolConstant.CONTROL_CHARACTER_LIST_END;
    }

    private int readStringLength(ByteBuffer in) {
        int result = 0;
        int shift = 0;

        while (true) {
            byte b = in.get();
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) != 128)
                break;
            shift += 7;
        }
        return result;
    }

}
