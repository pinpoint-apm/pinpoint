package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.registry.DefaultInterceptorRegistryAdaptor;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistryAdaptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class DefaultInterceptorRegistryAdaptorTest {

    @Test
    public void indexSize_0() {

        try {
            new DefaultInterceptorRegistryAdaptor(-1);
            Assertions.fail();
        } catch (IllegalArgumentException ignored) {
        }

    }

    @Test
    public void indexSize_1() {
        try {
            InterceptorRegistryAdaptor interceptorRegistry = new DefaultInterceptorRegistryAdaptor(0);
            StaticAroundInterceptor mock = mock(StaticAroundInterceptor.class);
            interceptorRegistry.addInterceptor(mock);
            Assertions.fail();
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void indexSize_2() {
        InterceptorRegistryAdaptor interceptorRegistry = new DefaultInterceptorRegistryAdaptor(1);
        interceptorRegistry.addInterceptor(mock(StaticAroundInterceptor.class));
        try {
            interceptorRegistry.addInterceptor(mock(StaticAroundInterceptor.class));
            Assertions.fail();
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void addStaticInterceptor() {
        StaticAroundInterceptor mock = mock(StaticAroundInterceptor.class);

        InterceptorRegistryAdaptor registry = new DefaultInterceptorRegistryAdaptor();
        int key = registry.addInterceptor(mock);
        Interceptor find = registry.getInterceptor(key);

        Assertions.assertSame(mock, find);
    }

    @Test
    public void addSimpleInterceptor() {
        AroundInterceptor mock = mock(AroundInterceptor.class);

        InterceptorRegistryAdaptor registry = new DefaultInterceptorRegistryAdaptor();
        int key = registry.addInterceptor(mock);
        Interceptor find = registry.getInterceptor(key);

        Assertions.assertSame(mock, find);
    }
}