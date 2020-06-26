package com.navercorp.pinpoint.profiler.context.errorhandler;

public interface ErrorHandler {

    boolean handleError(Throwable th);
}
