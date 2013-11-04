package com.nhn.pinpoint.profiler.util;


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
        // TODO limit음수일 경우 예외처리 필요.
        // size 4인 배열의 경 3에서 멈춰야 하므로 -1
        int iMax = bytes.length - 1;
        int iLimit = limit - 1;
        if (iMax > iLimit) {
            iMax = iLimit;
        }
        if (iMax == -1) {
            if (bytes.length == 0) {
                return "[]";
            } else {
                return "[...(" + bytes.length + ")]";
            }
        }


        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; ; i++) {
            sb.append(bytes[i]);
            if (i == iMax) {
                if (iMax < iLimit) {
                    return sb.append(']').toString();
                } else {
                    if (i > 0) {

                    }
                    sb.append(", ");
                    sb.append("...(");
                    sb.append(bytes.length);
                    sb.append(")]");
                    return sb.toString();
                }
            }
            sb.append(", ");
        }
    }


}
