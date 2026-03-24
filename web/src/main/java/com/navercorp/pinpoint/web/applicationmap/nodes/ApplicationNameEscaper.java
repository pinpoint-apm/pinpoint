package com.navercorp.pinpoint.web.applicationmap.nodes;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class ApplicationNameEscaper {

    private static final char ESCAPE_CHAR = '\\';
    private static final char DELIMITER = NodeName.NODE_DELIMITER_CHAR;

    private ApplicationNameEscaper() {
    }

    public static String escape(String applicationName) {
        StringBuilder sb = null;
        final int length = applicationName.length();
        for (int i = 0; i < length; i++) {
            char ch = applicationName.charAt(i);
            if (ch == ESCAPE_CHAR || ch == DELIMITER) {
                if (sb == null) {
                    sb = new StringBuilder(length + 4);
                    sb.append(applicationName, 0, i);
                }
                sb.append(ESCAPE_CHAR);
                sb.append(ch);
            } else if (sb != null) {
                sb.append(ch);
            }
        }
        return (sb != null) ? sb.toString() : applicationName;
    }

    public static String unescape(String escapedApplicationName) {
        StringBuilder sb = null;
        final int length = escapedApplicationName.length();
        for (int i = 0; i < length; i++) {
            char ch = escapedApplicationName.charAt(i);
            if (ch == ESCAPE_CHAR && i + 1 < length) {
                if (sb == null) {
                    sb = new StringBuilder(length);
                    sb.append(escapedApplicationName, 0, i);
                }
                i++;
                sb.append(escapedApplicationName.charAt(i));
            } else if (sb != null) {
                sb.append(ch);
            }
        }
        return (sb != null) ? sb.toString() : escapedApplicationName;
    }
}