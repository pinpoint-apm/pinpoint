package com.profiler.modifier.db.interceptor;

import com.profiler.interceptor.SimpleBeforeInterceptor;
import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.interceptor.util.JDBCScope;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.MetaObject;

import java.sql.Connection;
import com.profiler.logging.Logger;

public class ConnectionCloseInterceptor implements SimpleBeforeInterceptor {

    private final Logger logger = LoggerFactory.getLogger(ConnectionCloseInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final Object[] EMPTY = new Object[]{null};

    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
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
