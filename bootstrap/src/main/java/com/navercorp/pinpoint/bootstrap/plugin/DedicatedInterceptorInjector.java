package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;

public class DedicatedInterceptorInjector implements InterceptorInjector {
    private final String targetMethodName;
    private final String[] targetMethodParameterTypes;
    private final InterceptorFactory factory;

    public DedicatedInterceptorInjector(String targetMethodName, String[] targetMethodParameterTypes, InterceptorFactory factory) {
        this.targetMethodName = targetMethodName;
        this.targetMethodParameterTypes = targetMethodParameterTypes;
        this.factory = factory;
    }


    @Override
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        MethodInfo targetMethod = target.getDeclaredMethod(targetMethodName, targetMethodParameterTypes);
        Interceptor interceptor = factory.getInterceptor(classLoader, target, targetMethod); 
        target.addInterceptor(targetMethodName, targetMethodParameterTypes, interceptor);
    }

}
