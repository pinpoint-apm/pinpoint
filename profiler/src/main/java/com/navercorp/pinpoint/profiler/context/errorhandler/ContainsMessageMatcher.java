package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

public class ContainsMessageMatcher implements MessageMatcher {
    private final String[] patterns;

    public ContainsMessageMatcher(List<String> patterns) {
        Assert.requireNonNull(patterns, "patterns");
        this.patterns = patterns.toArray(new String[0]);
    }

    @Override
    public boolean match(String message) {
        if (message == null) {
            return false;
        }
        for (String pattern : patterns) {
            if (message.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
