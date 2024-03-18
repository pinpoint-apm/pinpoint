package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.registry.DefaultInterceptorRegistryAdaptor;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistryAdaptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class DefaultInterceptorRegistryAdaptorTest {

    @Test
    public void indexSize_0() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new DefaultInterceptorRegistryAdaptor(-1);
        });
    }

    @Test
    public void indexSize_1() {
        InterceptorRegistryAdaptor interceptorRegistry = new DefaultInterceptorRegistryAdaptor(0);
        StaticAroundInterceptor mock = mock(StaticAroundInterceptor.class);

        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            interceptorRegistry.addInterceptor(mock);
        });
    }

    @Test
    public void indexSize_2() {
        InterceptorRegistryAdaptor interceptorRegistry = new DefaultInterceptorRegistryAdaptor(1);
        interceptorRegistry.addInterceptor(mock(StaticAroundInterceptor.class));

        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> {
            interceptorRegistry.addInterceptor(mock(StaticAroundInterceptor.class));
        });
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