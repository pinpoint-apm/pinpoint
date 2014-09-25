package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.util.StringUtils;

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
                // 드롭된 bindValue를 생략하는 메시지를 첨부하면 bindValue를 통해 바인딩 sql 생성하기가 힘든면이 있으나 없으면 생략인지 아닌지 알수가 없어 수정.
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
