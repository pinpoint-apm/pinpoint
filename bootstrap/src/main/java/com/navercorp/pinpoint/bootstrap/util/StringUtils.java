package com.nhn.pinpoint.bootstrap.util;

public final class StringUtils {

    private StringUtils() {
    }

    public static String defaultString(final String str, final String defaultStr) {
        return str == null ? defaultStr : str;
    }

    public static String toString(final Object object) {
        if (object == null) {
            return "null";
        }
        return object.toString();
    }

    public static String drop(final String str) {
        return drop(str, 64);
    }

    public static String drop(final String str, final int length) {
        if (str == null) {
            return "null";
        }
        if (length < 0) {
            throw new IllegalArgumentException("negative length:" + length);
        }
        if (str.length() > length) {
            StringBuilder buffer = new StringBuilder(length + 10);
            buffer.append(str.substring(0, length));
            appendDropMessage(buffer, str.length());
            return buffer.toString();
        } else {
            return str;
        }
    }

    public static void appendDrop(StringBuilder builder, final String str, final int length) {
        if (str == null) {
            return;
        }
        if (length < 0) {
            return;
        }
        if (str.length() > length) {
            builder.append(str.substring(0, length));
            appendDropMessage(builder, str.length());
        } else {
            builder.append(str);
        }
    }

    private static void appendDropMessage(StringBuilder buffer, int length) {
        buffer.append("...(");
        buffer.append(length);
        buffer.append(')');
    }
}
