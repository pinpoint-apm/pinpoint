package com.navercorp.pinpoint.test.plugin.util;

public final class Assert {
    private Assert() {
    }

    public static <T> T requireNonNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }
}
