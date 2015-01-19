package com.navercorp.pinpoint.profiler.interceptor.bci;

import com.navercorp.pinpoint.bootstrap.instrument.ScopeDefinition;

/**
 * @author emeroad
 */
public class DefaultScopeDefinition implements ScopeDefinition {

    private final String name;
    private final ScopeType scopeType;

    public DefaultScopeDefinition(String name, ScopeType scopeType) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        if (scopeType == null) {
            throw new NullPointerException("scopeType must not be null");
        }
        this.name = name;
        this.scopeType = scopeType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ScopeType getType() {
        return scopeType;
    }
}
