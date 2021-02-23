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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc.bindvalue;

import java.util.Arrays;

/**
 * @author emeroad
 */
public class BytesConverter implements Converter {
    @Override
    public String convert(Object[] args) {
        if (args == null) {
            return "null";
        }
        if (args.length == 2) {
            final byte[] bytes = (byte[]) args[1];
            if (bytes == null) {
                return "null";
            } else {
                return toHexString(bytes, MAX_BYTES_SIZE);
            }
        }
        return "error";
    }

    // for uuid
    private static final int MAX_BYTES_SIZE = 16;

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
        'F' };

    private static String toHexString(byte[] bytes) {
        char[] result = new char[bytes.length * 2];
        int idx = 0;
        for (byte b : bytes) {
            int temp = (int) b & 0xFF;
            result[idx++] = HEX_CHARS[temp / 16];
            result[idx++] = HEX_CHARS[temp % 16];
        }
        return new String(result);
    }

    private static String toHexString(byte[] bytes, int maxSize) {
        if(bytes.length > 16) {
            return toHexString(Arrays.copyOf(bytes, maxSize)) + "...";
        }else {
            return toHexString(bytes);
        }
    }

}
