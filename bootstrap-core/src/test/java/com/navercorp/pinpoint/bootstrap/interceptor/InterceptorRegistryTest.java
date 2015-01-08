package com.navercorp.pinpoint.bootstrap.interceptor;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class InterceptorRegistryTest {

    @Test
    public void indexSize_0() {

        try {
            new InterceptorRegistry(-1);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {
        }

    }

    @Test
    public void indexSize_1() {
        try {
            InterceptorRegistry interceptorRegistry = new InterceptorRegistry(0);
            StaticAroundInterceptor mock = mock(StaticAroundInterceptor.class);
            interceptorRegistry.addStaticInterceptor(mock);
            Assert.fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    @Test
    public void indexSize_2() {
        InterceptorRegistry interceptorRegistry = new InterceptorRegistry(1);
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

        InterceptorRegistry registry = new InterceptorRegistry();
        int key = registry.addStaticInterceptor(mock);
        StaticAroundInterceptor find = registry.getInterceptor0(key);

        Assert.assertSame(mock, find);
    }

    @Test
     public void addSimpleInterceptor() {
        SimpleAroundInterceptor mock = mock(SimpleAroundInterceptor.class);

        InterceptorRegistry registry = new InterceptorRegistry();
        int key = registry.addSimpleInterceptor0(mock);
        SimpleAroundInterceptor find = registry.getSimpleInterceptor0(key);

        Assert.assertSame(mock, find);
    }

    @Test
    public void findInterceptor() {
        SimpleAroundInterceptor mock0 = mock(SimpleAroundInterceptor.class);
        StaticAroundInterceptor mock1 = mock(StaticAroundInterceptor.class);

        InterceptorRegistry registry = new InterceptorRegistry();
        int key1 = registry.addSimpleInterceptor0(mock0);
        Assert.assertEquals(key1, 0);
        int key2 = registry.addStaticInterceptor(mock1);
        Assert.assertEquals(key2, 1);
        Interceptor interceptor0 = registry.findInterceptor0(0);
        Assert.assertNotNull(interceptor0);
        Assert.assertSame(interceptor0, mock0);
        Interceptor interceptor1 = registry.findInterceptor0(1);
        Assert.assertNotNull(interceptor1);
        Assert.assertSame(interceptor1, mock1);

    }

}