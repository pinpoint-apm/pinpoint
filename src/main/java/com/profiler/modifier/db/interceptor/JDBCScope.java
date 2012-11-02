package com.profiler.modifier.db.interceptor;

import com.profiler.util.NamedThreadLocal;

/**
 *
 */
public class JDBCScope {
    private static NamedThreadLocal<Boolean> scope = new NamedThreadLocal<Boolean>("JDBCScope") {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public static void pushScope() {
        scope.set(Boolean.TRUE);
    }

    public static boolean isInternal() {
        return scope.get() != null;
    }

    public static void popScope() {
        scope.set(null);
    }

}
