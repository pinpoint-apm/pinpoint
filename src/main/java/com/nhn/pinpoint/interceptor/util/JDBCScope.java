package com.nhn.pinpoint.interceptor.util;

import com.nhn.pinpoint.util.Scope;

/**
 *
 */
public class JDBCScope {
    private static Scope scope = new Scope("JDBCScope");


    public static void push() {
        scope.push();
    }

    public static boolean isInternal() {
        return scope.isInternal();
    }

    public static void pop() {
        scope.pop();
    }

}
