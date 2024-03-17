package com.navercorp.pinpoint.profiler.context.errorhandler;


public class AlwaysMessageMatcher implements MessageMatcher {

    public AlwaysMessageMatcher() {
    }

    @Override
    public boolean match(String message) {
        return true;
    }

    @Override
    public String toString() {
        return "AlwaysMessageMatcher{}";
    }
}
