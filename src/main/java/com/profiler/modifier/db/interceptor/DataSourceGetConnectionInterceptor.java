package com.profiler.modifier.db.interceptor;

import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.StringUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Datasource의 get을 추적해야 될것으로 예상됨.
 */
public class DataSourceGetConnectionInterceptor implements StaticAroundInterceptor {

    private final Logger logger = Logger.getLogger(DataSourceGetConnectionInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
        }
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
        }

        if (!InterceptorUtils.isSuccess(result)) {
            return;
        }
        // TODO before도 같이 후킹하여 Connection 생성시간도 측정해야 됨.
        // datasource의 pool을 고려할것.
        if (result instanceof Connection) {

        }
    }

}
