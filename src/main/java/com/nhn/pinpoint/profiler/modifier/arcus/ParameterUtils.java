package com.nhn.pinpoint.profiler.modifier.arcus;

import com.nhn.pinpoint.profiler.interceptor.bci.Method;

/**
 *
 */
public class ParameterUtils {

    public static int findFirstString(Method method) {
        if (method == null) {
            return -1;
        }
        final String[] methodParams = method.getMethodParams();
        for(int i =0; i < methodParams.length; i++) {
            if ("java.lang.String".equals(methodParams[i])) {
                return i;
            }
        }
        return -1;
    }
}
