package com.navercorp.pinpoint.profiler.context.errorhandler;

public interface IgnoreErrorHandler {
    boolean handleError(Throwable th);
}
