package com.navercorp.pinpoint.bootstrap.interceptor.registry;

import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;


/**
 * @author emeroad
 */
public interface InterceptorRegistryAdaptor {
    int addInterceptor(Interceptor interceptor);
    Interceptor getInterceptor(int key);
}
