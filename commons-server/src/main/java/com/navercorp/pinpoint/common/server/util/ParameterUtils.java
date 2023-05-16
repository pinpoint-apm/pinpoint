package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.util.ArrayUtils;

public final class ParameterUtils {
    private ParameterUtils() {
    }

    public static String join(String[] parameters, String separator) {
        if (ArrayUtils.isEmpty(parameters)) {
            return "()";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(parameters[0]);
        for (int i = 1; i < parameters.length; i++) {
            sb.append(separator);
            sb.append(parameters[i]);
        }
        sb.append(')');
        return sb.toString();
    }
}
