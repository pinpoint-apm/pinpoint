package com.nhn.pinpoint.util;

import java.lang.reflect.Method;

public class IncludeBindVariableFilter implements BindVariableFilter {
    private String[] includes;

    public IncludeBindVariableFilter(String[] includes) {
        this.includes = includes;
    }

    @Override
    public boolean filter(Method method) {
        for (String include: includes) {
            if(method.getName().equals(include)) {
                return true;
            }
        }
        return false;
    }
}
