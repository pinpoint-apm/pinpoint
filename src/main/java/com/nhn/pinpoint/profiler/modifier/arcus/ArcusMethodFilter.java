package com.nhn.pinpoint.profiler.modifier.arcus;

import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;
import javassist.CtMethod;

/**
 *
 */
public class ArcusMethodFilter implements MethodFilter {
    private String[] ignoredPrefixes;

    public ArcusMethodFilter(String[] ignoredPrefixes) {
        this.ignoredPrefixes = ignoredPrefixes;
    }

    @Override
    public boolean filter(CtMethod ctMethod) {
        if (ctMethod.getModifiers() != javassist.Modifier.PUBLIC) {
            return true;
        }
        for (String ignoredPrefix : ignoredPrefixes) {
            if (ctMethod.getName().startsWith(ignoredPrefix)) {
                return true;
            }
        }
        return false;
    }
}
