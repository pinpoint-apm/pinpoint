package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.profiler.interceptor.ParameterExtractor;

/**
 *
 */
public class FirstStringParameterExtractor implements ParameterExtractor {
    @Override
    public int extractIndex(Object[] parameterList) {
        if (parameterList == null) {
            return NOT_FOUND;
        }
        for (int i = 0; i < parameterList.length; i++) {
            final Object current = parameterList[i];
            if (current instanceof String) {
                return i;
            }
        }
        return NOT_FOUND;
    }

    @Override
    public Object extractObject(Object[] parameterList, int index) {
        if (parameterList == null) {
            return NULL;
        }
        // index 레인지 체크 필요.
        final Object current = parameterList[index];
        if (current instanceof String) {
            return current;
        }
        return NULL;
    }
}
