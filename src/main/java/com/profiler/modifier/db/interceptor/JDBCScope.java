package com.profiler.modifier.db.interceptor;

import com.profiler.util.Scope;

/**
 *
 */
public class JDBCScope {
    private static Scope scope = new Scope("JDBCScope");


    public static void pushScope() {
        scope.pushScope();
    }

    public static boolean isInternal() {
        return scope.isInternal();
    }

    public static void popScope() {
        scope.popScope();
    }

}
