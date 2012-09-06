package com.profiler.util;

import javassist.CtClass;

public class JavaAssistUtils {
    private final static String NULL = "()";
    /**
     * test(int, java.lang.String) 일경우
     * (int, java.lang.String)로 생성된다.
     * @param params
     * @return
     */
    public static String getParameterDescription(CtClass[] params) {
        if(params == null) {
            return NULL;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append("(");
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getName());
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String getParameterDescription(Class[] params) {
        if(params == null) {
            return NULL;
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append("(");
        int end = params.length - 1;
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getName());
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
