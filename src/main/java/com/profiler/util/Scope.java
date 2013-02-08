package com.profiler.util;

/**
 *
 */
public class Scope {

    private final NamedThreadLocal<Boolean> scope;

    public Scope(final String scopeName) {
        this.scope = new NamedThreadLocal<Boolean>(scopeName);
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
