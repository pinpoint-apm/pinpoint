package com.nhn.pinpoint.profiler.modifier.redis.filter;

import java.lang.reflect.Modifier;
import java.util.Set;

import javassist.CtMethod;

import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;

/**
 * Name based on method filter
 * 
 * @author jaehong.kim
 *
 */
public class NameBasedMethodFilter implements MethodFilter {
    private static final int SYNTHETIC = 0x00001000;
    private final Set<String> methodNames;

    public NameBasedMethodFilter(final Set<String> methodNames) {
        this.methodNames = methodNames;
    }

    @Override
    public boolean filter(CtMethod ctMethod) {
        final int modifiers = ctMethod.getModifiers();

        if (isSynthetic(modifiers) || !Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers)) {
            return true;
        }

        if (methodNames.contains(ctMethod.getName())) {
            return false;
        }

        return true;
    }

    private boolean isSynthetic(int mod) {
        return (mod & SYNTHETIC) != 0;
    }
}