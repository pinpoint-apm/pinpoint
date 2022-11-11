package com.navercorp.pinpoint.plugin.cassandra4.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessorUtils;

public class DefaultSessionCloseInterceptor implements AroundInterceptor {

    @Override
    public void before(Object target, Object[] args) {
        // In case of close, we have to delete data even if the invocation failed.
        DatabaseInfoAccessorUtils.setDatabaseInfo(null, target);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
