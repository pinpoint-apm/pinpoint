package com.nhn.pinpoint.profiler.util;

import java.lang.reflect.Method;

/**
 * @author emeroad
 */
public class ExcludeBindVariableFilter implements BindVariableFilter {

    private String[] excudes;

    public ExcludeBindVariableFilter(String[] excludes) {
        if (excludes == null) {
            throw new NullPointerException("excludes must not be null");
        }
        this.excudes = excludes;
    }

    @Override
    public boolean filter(Method method) {
        if (method == null) {
            throw new NullPointerException("method must not be null");
        }
        for (String exclude : excudes) {
            if(method.getName().equals(exclude)) {
                return false;
            }
        }
        return true;
    }
}
