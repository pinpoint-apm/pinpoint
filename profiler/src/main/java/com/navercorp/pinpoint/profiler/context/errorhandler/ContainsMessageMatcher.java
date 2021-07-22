package com.navercorp.pinpoint.profiler.context.errorhandler;

import java.util.Arrays;
import java.util.Objects;

public class ContainsMessageMatcher implements MessageMatcher {
    private final String[] messagePatterns;

    public ContainsMessageMatcher(String[] messagePatterns) {
        this.messagePatterns = Objects.requireNonNull(messagePatterns, "messagePatterns");
    }

    @Override
    public boolean match(String message) {
        if (message == null) {
            return false;
        }
        for (String pattern : messagePatterns) {
            if (message.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ContainsMessageMatcher{" +
                "messagePatterns=" + Arrays.toString(messagePatterns) +
                '}';
    }
}
