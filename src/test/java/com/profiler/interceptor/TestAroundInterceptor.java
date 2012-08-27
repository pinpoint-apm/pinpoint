package com.profiler.interceptor;

public class TestAroundInterceptor implements StaticAroundInterceptor {

    public TestBeforeInterceptor before = new TestBeforeInterceptor();
    public TestAfterInterceptor after = new TestAfterInterceptor();

    @Override
    public void before(Object target, String className, String methodName, Object[] args) {
        before.before(target, className, methodName, args);
    }

    @Override
    public void after(Object target, String className, String methodName, Object[] args, Object result) {
        after.after(target, className, methodName, args, result);
    }


}
