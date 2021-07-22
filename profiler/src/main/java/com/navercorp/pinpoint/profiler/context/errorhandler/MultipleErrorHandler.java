package com.navercorp.pinpoint.profiler.context.errorhandler;

import java.util.Arrays;
import java.util.Objects;

public class MultipleErrorHandler implements IgnoreErrorHandler {
    private final IgnoreErrorHandler[] handlers;

    public MultipleErrorHandler(IgnoreErrorHandler[] handlers) {
        this.handlers = Objects.requireNonNull(handlers, "handlers");
    }

    @Override
    public boolean handleError(Throwable th) {
        for (IgnoreErrorHandler handler : handlers) {
            if (handler.handleError(th)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MultipleErrorHandler{" +
                "handlers=" + Arrays.toString(handlers) +
                '}';
    }
}
