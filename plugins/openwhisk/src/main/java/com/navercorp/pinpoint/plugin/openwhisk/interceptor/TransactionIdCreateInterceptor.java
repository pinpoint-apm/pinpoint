package com.navercorp.pinpoint.plugin.openwhisk.interceptor;


import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;


public class TransactionIdCreateInterceptor implements AroundInterceptor {


    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (args[0] instanceof AsyncContextAccessor) {
            ((AsyncContextAccessor) (result))._$PINPOINT$_setAsyncContext(AsyncContextAccessorUtils.getAsyncContext(args[0]));
        }
    }
}

