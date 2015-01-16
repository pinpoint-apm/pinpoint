package com.navercorp.pinpoint.bootstrap.interceptor;

import org.junit.Test;

import static org.junit.Assert.*;

public class InterceptorRegistryTest {

    @Test
    public void testBind() throws Exception {
        InterceptorRegistryAdaptor registryAdaptor = new DefaultInterceptorRegistryAdaptor();

        InterceptorRegistry.bind(registryAdaptor, null);
    }
}