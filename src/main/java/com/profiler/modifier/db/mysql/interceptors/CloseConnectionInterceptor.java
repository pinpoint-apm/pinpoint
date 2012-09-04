package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.modifier.db.ConnectionTrace;
import com.profiler.util.InterceptorUtils;

import java.sql.Connection;


public class CloseConnectionInterceptor implements StaticBeforeInterceptor {

    public void before(Object target, String className, String methodName, Object[] args) {
        if(!(target instanceof Connection)) {
            return;
        }

        ConnectionTrace connectionTrace = ConnectionTrace.getConnectionTrace();
        connectionTrace.closeConnection((Connection) target);

    }




}
