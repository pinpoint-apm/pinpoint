package com.profiler.modifier.db.interceptor;

import com.profiler.interceptor.SimpleAroundInterceptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.InterceptorUtils;

import java.sql.Connection;
import com.profiler.logging.Logger;

/**
 * Datasource의 get을 추적해야 될것으로 예상됨.
 */
public class DataSourceGetConnectionInterceptor implements SimpleAroundInterceptor {

    private final Logger logger = LoggerFactory.getLogger(DataSourceGetConnectionInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
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
