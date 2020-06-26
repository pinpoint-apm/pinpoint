package com.navercorp.pinpoint.profiler.context.errorhandler;


public class EmptyMessageMatcher implements MessageMatcher {

    public static final MessageMatcher EMPTY_MESSAGE_MATCHER = new EmptyMessageMatcher();

    private EmptyMessageMatcher() {
    }

    @Override
    public boolean match(String message) {
        return true;
    }

    @Override
    public String toString() {
        return "EmptyMessageMatcher{}";
    }
}
