package com.nhn.pinpoint.profiler.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class ScopePool {

    private final ConcurrentMap<String, Scope> pool = new ConcurrentHashMap<String, Scope>();

    public Scope getScope(String scopeName) {
        if (scopeName == null) {
            throw new NullPointerException("scopeName must not be null");
        }
        final Scope scope = this.pool.get(scopeName);
        if (scope != null) {
            return scope;
        }
        final Scope newScope = new DepthScope(scopeName);
        final Scope exist = this.pool.putIfAbsent(scopeName, newScope);
        if (exist != null) {
            return exist;
        }
        return newScope;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScopePool{");
        sb.append("pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}
