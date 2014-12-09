package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;

public class ConditionalInterceptorInjector implements InterceptorInjector {
    private final Condition condition;
    private final InterceptorInjector delegate;
    
    public ConditionalInterceptorInjector(Condition condition, InterceptorInjector delegate) {
        this.condition = condition;
        this.delegate = delegate;
    }

    @Override
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        if (condition.check(target)) {
            delegate.inject(classLoader, target);
        }
    }
}
