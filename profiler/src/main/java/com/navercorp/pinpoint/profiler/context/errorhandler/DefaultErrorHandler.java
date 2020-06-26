package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;

public class DefaultErrorHandler implements ErrorHandler {
    // for debug
    private final String errorHandlerName;
    private final ThrowableMatcher throwableMatcher;
    private final MessageMatcher messageMatcher;

    public DefaultErrorHandler(String errorHandlerName, ThrowableMatcher throwableMatcher, MessageMatcher messageMatcher) {
        this.errorHandlerName = Assert.requireNonNull(errorHandlerName, "errorHandlerName");
        this.throwableMatcher = Assert.requireNonNull(throwableMatcher, "throwableMatcher");
        this.messageMatcher = Assert.requireNonNull(messageMatcher, "messageMatcher");
    }

    @Override
    public boolean handleError(Throwable th) {
        if (throwableMatcher.match(th)) {
            if (messageMatcher.match(th.getMessage())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "DefaultErrorHandler{" +
                "errorHandlerName='" + errorHandlerName + '\'' +
                ", throwableMatcher=" + throwableMatcher +
                ", messageMatcher=" + messageMatcher +
                '}';
    }
}
