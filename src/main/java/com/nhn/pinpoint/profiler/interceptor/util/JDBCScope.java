package com.nhn.pinpoint.profiler.interceptor.util;

import com.nhn.pinpoint.profiler.util.Scope;

/**
 *
 */
public class JDBCScope {
    private static final Scope SCOPE = new Scope("JDBCScope");


    public static void push() {
        SCOPE.push();
    }

    public static boolean isInternal() {
        return SCOPE.isInternal();
    }

    public static void pop() {
        SCOPE.pop();
    }

}
