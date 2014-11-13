package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;

public class ConstructorInterceptorInjector implements InterceptorInjector {
    private final String[] targetParameterTypes;
    private final InterceptorFactory factory;
    
    public ConstructorInterceptorInjector(String[] targetParameterTypes, InterceptorFactory factory) {
        this.targetParameterTypes = targetParameterTypes;
        this.factory = factory;
    }

    @Override
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        MethodInfo targetMethod = target.getConstructor(targetParameterTypes);
        Interceptor interceptor = factory.getInterceptor(classLoader, target, targetMethod);
        target.addConstructorInterceptor(targetParameterTypes, interceptor);
    }
}
