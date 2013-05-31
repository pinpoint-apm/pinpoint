package com.nhn.pinpoint.util;

import java.lang.reflect.Method;

public class ExcludeBindVariableFilter implements BindVariableFilter {

    private String[] excudes;

    public ExcludeBindVariableFilter(String[] excludes) {
        this.excudes = excludes;
    }

    @Override
    public boolean filter(Method method) {
        for (String exclude : excudes) {
            if(method.getName().equals(exclude)) {
                return false;
            }
        }
        return true;
    }
}
