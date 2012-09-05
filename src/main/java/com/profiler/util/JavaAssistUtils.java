package com.profiler.util;

import javassist.CtClass;

public class JavaAssistUtils {
    /**
     * test(int, java.lang.String) 일경우
     * (int, java.lang.String)로 생성된다.
     * @param params
     * @return
     */
    public static String getParameterDescription(CtClass[] params) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                sb.append(params[i].getName());
                if (i < (params.length - 1))
                    sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String getParameterDescription(Class[] params) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                sb.append(params[i].getName());
                if (i < (params.length - 1))
                    sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
