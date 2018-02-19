package com.navercorp.pinpoint.plugin.akka.http.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

public class RequestContextImplCopyInterceptor implements AroundInterceptor {
    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(AsyncContextAccessorUtils.getAsyncContext(target));
    }
}
