package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

public class ClassNameThrowableMatcher implements ThrowableMatcher {
    private final String[] classPatterns;

    public ClassNameThrowableMatcher(List<String> classPatterns) {
        Assert.requireNonNull(classPatterns, "classPattern");
        this.classPatterns = classPatterns.toArray(new String[0]);
    }

    public boolean match(Throwable th) {
        if (th == null) {
            return false;
        }
        final String name = th.getClass().getName();
        for (String classPattern : classPatterns) {
            if (classPattern.equals(name)) {
                return true;
            }
        }

        return false;
    }
}
