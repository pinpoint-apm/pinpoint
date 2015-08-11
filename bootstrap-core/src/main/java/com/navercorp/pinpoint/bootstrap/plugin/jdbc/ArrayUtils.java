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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

/**
 * @author emeroad
 */
public final class ArrayUtils {

    private ArrayUtils() {
    }

    public static String dropToString(byte[] bytes) {
        return dropToString(bytes, 32);
    }

    public static String dropToString(byte[] bytes, int limit) {
        if (bytes == null) {
            return "null";
        }
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        // TODO handle negative limit
        
        // Last valid index is length - 1
        int bytesMaxLength = bytes.length - 1;
        final int maxLimit = limit - 1;
        if (bytesMaxLength > maxLimit) {
            bytesMaxLength = maxLimit;
        }
        if (bytesMaxLength == -1) {
            if (bytes.length == 0) {
                return "[]";
            } else {
                return "[...(" + bytes.length + ")]";
            }
        }


        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; ; i++) {
            sb.append(bytes[i]);
            if (i == bytesMaxLength) {
                if ((bytes.length - 1) <= maxLimit) {
                    return sb.append(']').toString();
                } else {
                    sb.append(", ...(");
                    sb.append(bytes.length - (i+1));
                    sb.append(")]");
                    return sb.toString();
                }
            }
            sb.append(", ");
        }
    }


}
