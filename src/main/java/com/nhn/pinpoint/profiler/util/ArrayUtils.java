package com.nhn.pinpoint.profiler.util;

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
        // TODO limit음수일 경우 예외처리 필요.
        // size 4인 배열의 경 3에서 멈춰야 하므로 -1
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
