package com.navercorp.pinpoint.profiler.context.errorhandler;

public interface ThrowableMatcher {
    boolean match(Class<? extends Throwable> throwableClass);
}
