package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import java.sql.Connection;
import java.util.Arrays;

import com.nhn.pinpoint.profiler.interceptor.StaticAroundInterceptor;
import com.nhn.pinpoint.profiler.logging.PLogger;

import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.util.InterceptorUtils;
import com.nhn.pinpoint.profiler.util.MetaObject;
import com.nhn.pinpoint.profiler.util.StringUtils;

/**
 * @author emeroad
 */
@Deprecated
public class ConnectionCreateInterceptor implements StaticAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    // setUrl에서 String type은 databaseInfo로 변경되었다.
//    private final MetaObject setUrl = new MetaObject("__setDatabaseInfo", Object.class);

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, className, methodName, parameterDescription, args, result);
        }
        if (!InterceptorUtils.isSuccess(result)) {
            return;
        }
        // datasource의 pool을 고려할것.
        if (result instanceof Connection) {
            Object url = args[4];
            if (url instanceof String) {
//                this.setUrl.invoke(result, url);
            }
        }
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, className, methodName, parameterDescription, args);
        }
    }
}
