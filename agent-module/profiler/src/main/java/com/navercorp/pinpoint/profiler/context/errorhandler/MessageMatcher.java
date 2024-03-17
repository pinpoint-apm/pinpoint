package com.navercorp.pinpoint.profiler.context.errorhandler;

public interface MessageMatcher {
    boolean match(String message);
}
