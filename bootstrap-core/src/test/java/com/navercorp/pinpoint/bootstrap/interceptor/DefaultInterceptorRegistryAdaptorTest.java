package com.navercorp.pinpoint.bootstrap.interceptor;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class DefaultInterceptorRegistryAdaptorTest {

    @Test
    public void indexSize_0() {

        try {
            new DefaultInterceptorRegistryAdaptor(-1);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {
        }

    }

    @Test
    public void indexSize_1() {
        try {
            InterceptorRegistryAdaptor interceptorRegistry = new DefaultInterceptorRegistryAdaptor(0);
            StaticAroundInterceptor mock = mock(StaticAroundInterceptor.class);
            interceptorRegistry.addStaticInterceptor(mock);
            Assert.fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    @Test
    public void indexSize_2() {
        InterceptorRegistryAdaptor interceptorRegistry = new DefaultInterceptorRegistryAdaptor(1);
        interceptorRegistry.addStaticInterceptor(mock(StaticAroundInterceptor.class));
        try {
            interceptorRegistry.addStaticInterceptor(mock(StaticAroundInterceptor.class));
            Assert.fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    @Test
    public void addStaticInterceptor()  {
        StaticAroundInterceptor mock = mock(StaticAroundInterceptor.class);

        InterceptorRegistryAdaptor registry = new DefaultInterceptorRegistryAdaptor();
        int key = registry.addStaticInterceptor(mock);
        StaticAroundInterceptor find = registry.getStaticInterceptor(key);

        Assert.assertSame(mock, find);
    }

    @Test
     public void addSimpleInterceptor() {
        SimpleAroundInterceptor mock = mock(SimpleAroundInterceptor.class);

        InterceptorRegistryAdaptor registry = new DefaultInterceptorRegistryAdaptor();
        int key = registry.addSimpleInterceptor(mock);
        SimpleAroundInterceptor find = registry.getSimpleInterceptor(key);

        Assert.assertSame(mock, find);
    }
}