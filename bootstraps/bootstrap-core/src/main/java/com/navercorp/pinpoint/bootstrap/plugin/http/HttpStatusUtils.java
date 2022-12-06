package com.navercorp.pinpoint.bootstrap.plugin.http;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class HttpStatusUtils {
    static final int SC_INFORMATIONAL_PREFIX = 1;
    static final int SC_SUCCESS_PREFIX = 2;
    static final int SC_REDIRECTION_PREFIX = 3;
    static final int SC_CLIENT_ERROR_PREFIX = 4;
    static final int SC_SERVER_ERROR_PREFIX = 5;
    
    private HttpStatusUtils() {
    }

    public static boolean isError(int statusCode) {
        final int prefix = toPrefix(statusCode);
        switch (prefix) {
            case SC_CLIENT_ERROR_PREFIX:
            case SC_SERVER_ERROR_PREFIX:
                return true;
        }

        return false;
    }

    public static boolean isNonError(int statusCode) {
        final int prefix = toPrefix(statusCode);
        switch (prefix) {
            case SC_INFORMATIONAL_PREFIX:
            case SC_SUCCESS_PREFIX:
            case SC_REDIRECTION_PREFIX:
                return true;
        }

        return false;
    }

    private static int toPrefix(int statusCode) {
        return statusCode / 100;
    }
}
