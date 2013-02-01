package com.profiler.modifier.db.interceptor;

import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Logger;

public class ConnectionCloseInterceptor implements StaticBeforeInterceptor {

    private final Logger logger = Logger.getLogger(ConnectionCloseInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    private static final Object[] EMPTY = new Object[]{null};

    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
        }
        if (JDBCScope.isInternal()) {
            logger.info("internal jdbc scope. skip trace");
            return;
        }
        // close의 경우 호출이 실패하더라도 데이터를 삭제해야함.
        if (target instanceof Connection) {
            this.setUrl.invoke(target, EMPTY);
        }
    }
}
