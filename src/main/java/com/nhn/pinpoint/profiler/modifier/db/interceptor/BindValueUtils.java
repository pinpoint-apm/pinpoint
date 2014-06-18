package com.nhn.pinpoint.profiler.modifier.db.interceptor;

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
            sb.append(bindValueArray[i]);
            if (sb.length() > limit) {
                // 여기서 드롭된 bindValue를 생략하는 메시지를 첨부하면 bindValue를 통해 반인딩 sql 생성하기가 힘듬.
                break;
            }
            if (i < end) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public static String bindValueToString(String[] stringArray) {
        return bindValueToString(stringArray, Integer.MAX_VALUE);
    }
}
