package com.navercorp.pinpoint.profiler.context.errorhandler;

import java.util.Arrays;
import java.util.Objects;


public class ClassNameThrowableMatcher implements ThrowableMatcher {
    private final String[] classPatterns;

    public ClassNameThrowableMatcher(String[] classPatterns) {
        this.classPatterns = Objects.requireNonNull(classPatterns, "classPattern");
    }


    public boolean match(Class<? extends Throwable> throwableClass) {
        if (throwableClass == null) {
            return false;
        }
        final String name = throwableClass.getName();
        for (String classPattern : classPatterns) {
            if (classPattern.equals(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "ClassNameThrowableMatcher{" +
                "classPatterns=" + Arrays.toString(classPatterns) +
                '}';
    }
}
