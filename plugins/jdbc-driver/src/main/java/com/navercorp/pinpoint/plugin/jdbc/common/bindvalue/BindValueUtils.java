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

package com.navercorp.pinpoint.plugin.jdbc.common.bindvalue;

import com.navercorp.pinpoint.bootstrap.util.StringUtils;

/**
 * @author emeroad
 */
public class BindValueUtils {

    private BindValueUtils() {
    }

    public static String bindValueToString(String[] bindValueArray, int limit) {
        if (bindValueArray == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(32);
        final int length = bindValueArray.length;
        final int end = length - 1;
        for (int i = 0; i < length; i++) {
            if (sb.length() >= limit) {
                // Appending omission postfix makes generating binded sql difficult. But without this, we cannot say if it's omitted or not.
                appendLength(sb, length);
                break;
            }
            StringUtils.appendDrop(sb, bindValueArray[i], limit);
            if (i < end) {
                sb.append(", ");
            }

        }
        return sb.toString();
    }

    private static void appendLength(StringBuilder sb, int length) {
        sb.append("...(");
        sb.append(length);
        sb.append(')');
    }

    public static String bindValueToString(String[] stringArray) {
        return bindValueToString(stringArray, Integer.MAX_VALUE);
    }
}
