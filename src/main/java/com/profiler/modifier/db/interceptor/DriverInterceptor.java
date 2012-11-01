package com.profiler.modifier.db.interceptor;

import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class DriverInterceptor implements StaticAroundInterceptor {

    private final Logger logger = Logger.getLogger(ConnectionCreateInterceptor.class.getName());
    private final MetaObject setUrl = new MetaObject("__setUrl", String.class);

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        JDBCScope.pushScope();
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        JDBCScope.popScope();
        // TODO 생성 시간 측정시 아래 코드를 다시 생각해야 됨.
        if (!InterceptorUtils.isSuccess(result)) {
            return;
        }
        // TODO before도 같이 후킹하여 Connection 생성시간도 측정해야 됨.
        // datasource의 pool을 고려할것.
        if (result instanceof Connection) {
            Object url = args[0];
            if (url instanceof String) {
                this.setUrl.invoke(result, url);
            }
        }
    }


}
