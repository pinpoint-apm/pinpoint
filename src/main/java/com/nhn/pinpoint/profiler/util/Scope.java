package com.nhn.pinpoint.profiler.util;

/**
 *
 */
public class Scope {

    private final NamedThreadLocal<Boolean> scope;

    public Scope(final String scopeName) {
        this.scope = new NamedThreadLocal<Boolean>(scopeName);
    }

    public void push() {
        scope.set(Boolean.TRUE);
    }

    public boolean isInternal() {
        return scope.get() != null;
    }

    public void pop() {
        scope.set(null);
    }
}
