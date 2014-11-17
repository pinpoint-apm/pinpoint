package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;

public class ConditionalClassEditor implements ClassEditor {
    private final Condition condition;
    private final ClassEditor delegate;
    
    public ConditionalClassEditor(Condition condition, ClassEditor delegate) {
        this.condition = condition;
        this.delegate = delegate;
    }

    @Override
    public byte[] edit(ClassLoader classLoader, InstrumentClass target) {
        if (condition.check(target)) {
            return delegate.edit(classLoader, target);
        }
        
        return null;
    }
}
