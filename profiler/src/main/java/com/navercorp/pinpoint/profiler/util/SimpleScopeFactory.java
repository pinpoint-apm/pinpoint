package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.instrument.Scope;

/**
 * @author emeroad
 */
public class SimpleScopeFactory implements ScopeFactory {

    private final String name;

    public SimpleScopeFactory(String name) {
        this.name = name;
    }

    @Override
    public Scope createScope() {
        return new SimpleScope(name);
    }

    @Override
    public String getName() {
        return name;
    }
}
