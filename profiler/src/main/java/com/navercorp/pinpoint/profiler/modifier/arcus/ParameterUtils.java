package com.nhn.pinpoint.profiler.modifier.arcus;

import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;

/**
 * @author emeroad
 */
public class ParameterUtils {

    public static int findFirstString(MethodInfo method, int maxIndex) {
        if (method == null) {
            return -1;
        }
        final String[] methodParams = method.getParameterTypes();
        final int minIndex = Math.min(methodParams.length, maxIndex);
        for(int i =0; i < minIndex; i++) {
            if ("java.lang.String".equals(methodParams[i])) {
                return i;
            }
        }
        return -1;
    }
}
