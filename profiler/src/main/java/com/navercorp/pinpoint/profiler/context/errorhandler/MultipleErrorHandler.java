package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.Arrays;
import java.util.List;

public class MultipleErrorHandler implements ErrorHandler {
    private final ErrorHandler[] handlers;

    public MultipleErrorHandler(List<ErrorHandler> handlers) {
        Assert.requireNonNull(handlers, "handlers");
        this.handlers = handlers.toArray(new ErrorHandler[0]);
    }

    @Override
    public boolean handleError(Throwable th) {
        for (ErrorHandler handler : handlers) {
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
