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

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Map;

/**
 * duplicate : com.navercorp.pinpoint.profiler.modifier.db.interceptor.BindValueUtils
 * @author emeroad
 */
public final class BindValueUtils {

    private BindValueUtils() {
    }

    public static String bindValueToString(final Map<Integer, String> bindValueMap, int limit) {
        if (bindValueMap == null) {
            return "";
        }
        if (bindValueMap.isEmpty()) {
            return "";
        }
        final int maxParameterIndex = getMaxParameterIndex(bindValueMap);
        if (maxParameterIndex <= 0) {
            return "";
        }
        final String[] temp = new String[maxParameterIndex];
        for (Map.Entry<Integer, String> entry : bindValueMap.entrySet()) {
            final int parameterIndex = entry.getKey() - 1;
            if (parameterIndex < 0) {
                // invalid index. PreparedStatement first parameterIndex is 1
                continue;
            }
            if (temp.length <= parameterIndex) {
                continue;
            }
            temp[parameterIndex] = entry.getValue();
        }
        return bindValueToString(temp, limit);
    }

    private static int getMaxParameterIndex(Map<Integer, String> bindValueMap) {
        int maxIndex = 0;
        for (Integer idx : bindValueMap.keySet()) {
            maxIndex = Math.max(maxIndex, idx);
        }
        return maxIndex;
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
            final String bindValue = StringUtils.defaultString(bindValueArray[i], "");
            StringUtils.appendAbbreviate(sb, bindValue, limit);
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
