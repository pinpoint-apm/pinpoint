package com.profiler.modifier.db.interceptor;

import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.SimpleAfterInterceptor;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.TraceContextSupport;
import com.profiler.interceptor.util.JDBCScope;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.LoggingUtils;
import com.profiler.modifier.db.DatabaseInfo;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;

import java.sql.Connection;
import com.profiler.logging.Logger;

public class StatementCreateInterceptor implements SimpleAfterInterceptor, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(StatementCreateInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    // connection 용.
    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");

    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);
    private TraceContext traceContext;

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }
        if (JDBCScope.isInternal()) {
            logger.debug("internal jdbc scope. skip trace");
            return;
        }
        if (!InterceptorUtils.isSuccess(result)) {
            return;
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        if (target instanceof Connection) {
            DatabaseInfo databaseInfo = (DatabaseInfo) getUrl.invoke(target);
            setUrl.invoke(result, databaseInfo);
        }
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}
