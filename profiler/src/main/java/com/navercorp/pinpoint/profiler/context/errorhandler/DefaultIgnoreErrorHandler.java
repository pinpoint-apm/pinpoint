package com.navercorp.pinpoint.profiler.context.errorhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DefaultIgnoreErrorHandler implements IgnoreErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // for debug
    private final String errorHandlerName;
    private final ThrowableMatcher throwableMatcher;
    private final MessageMatcher messageMatcher;

    public DefaultIgnoreErrorHandler(String errorHandlerName, ThrowableMatcher throwableMatcher, MessageMatcher messageMatcher) {
        this.errorHandlerName = Objects.requireNonNull(errorHandlerName, "errorHandlerName");
        this.throwableMatcher = Objects.requireNonNull(throwableMatcher, "throwableMatcher");
        this.messageMatcher = Objects.requireNonNull(messageMatcher, "messageMatcher");
    }

    @Override
    public boolean handleError(Throwable th) {
        if (th == null) {
            return false;
        }
        Class<? extends Throwable> thClass = th.getClass();
        if (throwableMatcher.match(thClass)) {
            if (messageMatcher.match(th.getMessage())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignore Exception th:{} handler:{},", th.toString(), this);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "DefaultIgnoreErrorHandler{" +
                "errorHandlerName='" + errorHandlerName + '\'' +
                ", throwableMatcher=" + throwableMatcher +
                ", messageMatcher=" + messageMatcher +
                '}';
    }
}
