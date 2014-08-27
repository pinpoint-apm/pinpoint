package com.nhn.pinpoint.profiler.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;

/**
 * @author emeroad
 */
public class TestAroundInterceptor implements StaticAroundInterceptor {

    public TestBeforeInterceptor before = new TestBeforeInterceptor();
    public TestAfterInterceptor after = new TestAfterInterceptor();

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        before.before(target, className, methodName, parameterDescription, args);
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
        after.after(target, className, methodName, parameterDescription, args, result, throwable);
    }


}
