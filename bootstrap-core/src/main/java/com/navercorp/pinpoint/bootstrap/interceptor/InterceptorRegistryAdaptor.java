package com.navercorp.pinpoint.bootstrap.interceptor;


/**
 * @author emeroad
 */
public interface InterceptorRegistryAdaptor {

    int addStaticInterceptor(StaticAroundInterceptor interceptor);

    int addSimpleInterceptor(SimpleAroundInterceptor interceptor);
    
    int addInterceptor(InterceptorInstance interceptor);
    
    StaticAroundInterceptor getStaticInterceptor(int key);

    InterceptorInstance findInterceptor(int key);

    SimpleAroundInterceptor getSimpleInterceptor(int key);

}
