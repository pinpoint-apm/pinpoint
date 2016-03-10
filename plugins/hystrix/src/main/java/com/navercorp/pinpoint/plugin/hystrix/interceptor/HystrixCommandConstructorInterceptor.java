package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;

/**
 * @author Jiaqi Feng
 */
public class HystrixCommandConstructorInterceptor implements AroundInterceptor2 {
    //TODO reference WorkerConstructorInterceptor in pinpoint plugin sample
    private final InterceptorScope scope;

    public HystrixCommandConstructorInterceptor(InterceptorScope scope) {
        this.scope = scope;
    }

    @IgnoreMethod
    @Override
    public void before(Object target, Object arg0, Object arg1) {

    }

    @Override
    public void after(Object target, Object arg0, Object arg1, Object result, Throwable throwable) {
    }
}
