package com.navercorp.pinpoint.profiler.context.errorhandler;

public interface ThrowableMatcher {
    boolean match(Throwable th);
}
