package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.Arrays;

public class MultipleErrorHandler implements IgnoreErrorHandler {
    private final IgnoreErrorHandler[] handlers;

    public MultipleErrorHandler(IgnoreErrorHandler[] handlers) {
        this.handlers = Assert.requireNonNull(handlers, "handlers");
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
