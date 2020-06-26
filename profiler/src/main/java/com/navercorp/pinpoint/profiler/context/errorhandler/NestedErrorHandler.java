package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;

public class NestedErrorHandler implements ErrorHandler {
    private final ErrorHandler errorHandler;
    ;

    public NestedErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = Assert.requireNonNull(errorHandler, "errorHandler");
    }

    @Override
    public boolean handleError(Throwable th) {
        while (th != null) {
            if (this.errorHandler.handleError(th)) {
                return true;
            }
            th = th.getCause();
        }
        return false;
    }

    @Override
    public String toString() {
        return "ChainedErrorHandler{" +
                "errorHandler=" + errorHandler +
                '}';
    }
}
