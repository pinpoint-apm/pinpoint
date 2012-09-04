package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticBeforeInterceptor;

import java.sql.Connection;

public class CreateStatementInterceptor implements StaticAfterInterceptor {

    @Override
    public void after(Object target, String className, String methodName, Object[] args, Object result) {
        if (Trace.getCurrentTraceId() == null) {
            return;
        }
    }
}
