package com.profiler.util;

/**
 *
 */
public class Scope {

    private NamedThreadLocal<Boolean> scope = new NamedThreadLocal<Boolean>("JDBCScope");

    public Scope(String threadLocalName) {
        this.scope = new NamedThreadLocal<Boolean>(threadLocalName);
    }

    public void pushScope() {
        scope.set(Boolean.TRUE);
    }

    public boolean isInternal() {
        return scope.get() != null;
    }

    public void popScope() {
        scope.set(null);
    }
}
