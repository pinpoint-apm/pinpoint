package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.util.MetaObject;

import java.sql.Connection;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

/**
 * @author emeroad
 */
public class ConnectionCloseInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final Object[] EMPTY = new Object[]{null};

    private final MetaObject setUrl = new MetaObject("__setDatabaseInfo", Object.class);

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        // close의 경우 호출이 실패하더라도 데이터를 삭제해야함.
        if (target instanceof Connection) {
            this.setUrl.invoke(target, EMPTY);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
