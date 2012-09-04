package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.modifier.db.ConnectionTrace;
import com.profiler.util.InterceptorUtils;

import java.sql.Connection;

public class CreateConnectionInterceptor implements StaticAfterInterceptor {

    @Override
    public void after(Object target, String className, String methodName, Object[] args, Object result) {
        if (InterceptorUtils.isThrowable(result)) {
            return;
        }

        if (!(result instanceof Connection)) {
            return;
        }
        String url = (String) args[4];
        if (url instanceof String) {
            ConnectionTrace connectionTrace = ConnectionTrace.getConnectionTrace();
            connectionTrace.createConnection((Connection)result, url);
        }
    }
}
