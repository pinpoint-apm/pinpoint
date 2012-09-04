package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.modifier.db.ConnectionTrace;
import com.profiler.util.InterceptorUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CloseConnectionInterceptor implements StaticBeforeInterceptor {

    private final Logger logger = Logger.getLogger(CloseConnectionInterceptor.class.getName());

    public void before(Object target, String className, String methodName, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before className:" + className + " methodName:" + methodName + " args:" + Arrays.toString(args));
        }
        if (target instanceof Connection) {
            ConnectionTrace connectionTrace = ConnectionTrace.getConnectionTrace();
            connectionTrace.closeConnection((Connection) target);
        }
    }




}
