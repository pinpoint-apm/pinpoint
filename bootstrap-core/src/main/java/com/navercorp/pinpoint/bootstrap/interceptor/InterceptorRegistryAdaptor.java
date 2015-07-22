package com.navercorp.pinpoint.bootstrap.interceptor;

/**
 * @author emeroad
 */
public interface InterceptorRegistryAdaptor {

    int addStaticInterceptor(StaticAroundInterceptor interceptor);

    int addSimpleInterceptor(SimpleAroundInterceptor interceptor);
    
    int addInterceptor(Interceptor interceptor);

    StaticAroundInterceptor getStaticInterceptor(int key);

    Interceptor findInterceptor(int key);

    SimpleAroundInterceptor getSimpleInterceptor(int key);

}
