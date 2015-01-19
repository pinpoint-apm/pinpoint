package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.instrument.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.instrument.AttachmentScope;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;

/**
 * @author emeroad
 */
public class AttachmentSimpleScopeFactory<T> implements ScopeFactory {

    private final String name;

    public AttachmentSimpleScopeFactory(String name) {
        this.name = name;
    }

    @Override
    public Scope createScope() {
        return new AttachmentSimpleScope(name);
    }

    @Override
    public String getName() {
        return name;
    }
}
