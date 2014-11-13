package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.instrument.MethodFilter;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;

public class FilteringInterceptorInjector implements InterceptorInjector {
    private final MethodFilter filter;
    private final InterceptorFactory factory;
    private final boolean singletonInterceptor;
    
    public FilteringInterceptorInjector(MethodFilter filter, InterceptorFactory factory, boolean singletonInterceptor) {
        this.filter = filter;
        this.factory = factory;
        this.singletonInterceptor = singletonInterceptor;
    }
    
    @Override
    public void inject(ClassLoader classLoader, InstrumentClass target) throws InstrumentException {
        int interceptorId = -1;
        
        for (MethodInfo methodInfo : target.getDeclaredMethods(filter)) {
            String targetMethodName = methodInfo.getName();
            String[] targetParameterTypes = methodInfo.getParameterTypes();
            
            if (singletonInterceptor && interceptorId != -1) {
                target.reuseInterceptor(targetMethodName, targetParameterTypes, interceptorId);
            } else {
                Interceptor interceptor = factory.getInterceptor(classLoader, target, methodInfo);
                interceptorId = target.addInterceptor(targetMethodName, targetParameterTypes, interceptor);
            }
        }
    }
}
