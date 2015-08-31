package com.navercorp.pinpoint.bootstrap.interceptor;


/**
 * @author emeroad
 */
public interface InterceptorRegistryAdaptor {
    int addInterceptor(Interceptor interceptor);
    Interceptor getInterceptor(int key);
}
