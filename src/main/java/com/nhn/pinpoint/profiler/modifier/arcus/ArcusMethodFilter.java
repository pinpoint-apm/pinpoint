package com.nhn.pinpoint.profiler.modifier.arcus;

import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;
import javassist.CtMethod;

import java.lang.reflect.Modifier;

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
        final int modifiers = ctMethod.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) {
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
