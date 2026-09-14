package com.navercorp.pinpoint.alarm.util;

import java.util.Objects;

public final class ExceptionMessageUtils {

    private ExceptionMessageUtils() {
    }

    public static String rootCauseMessage(Throwable failure) {
        Throwable root = Objects.requireNonNull(failure, "failure");
        for (Throwable current = failure; current != null; current = current.getCause()) {
            root = current;
        }
        String message = root.getMessage();
        return message == null || message.isBlank() ? root.getClass().getSimpleName() : message;
    }
}
