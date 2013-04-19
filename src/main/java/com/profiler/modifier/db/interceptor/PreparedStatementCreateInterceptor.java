package com.profiler.modifier.db.interceptor;

import com.profiler.common.util.ParsingResult;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.*;
import com.profiler.interceptor.util.JDBCScope;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.LoggingUtils;
import com.profiler.modifier.db.DatabaseInfo;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;

import java.sql.Connection;
import com.profiler.logging.Logger;

public class PreparedStatementCreateInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(PreparedStatementCreateInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor descriptor;

    // connection 용.
    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");
    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);

    private final MetaObject setSql = new MetaObject("__setSql", Object.class);
    private int apiId;
    private TraceContext traceContext;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, className, methodName, parameterDescription, args);
        }
        if (JDBCScope.isInternal()) {
            logger.debug("internal jdbc scope. skip trace");
            return;
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        trace.markBeforeTime();

        DatabaseInfo databaseInfo = (DatabaseInfo) getUrl.invoke(target);

        trace.recordServiceType(databaseInfo.getType());

        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());
        trace.recordDestinationAddress(databaseInfo.getHost());

    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, className, methodName, parameterDescription, args, result);
        }
        if (JDBCScope.isInternal()) {
            logger.debug("internal jdbc scope. skip trace");
            return;
        }
        if (!InterceptorUtils.isSuccess(result)) {
            return;
            // TODO traceBlockEnd() 호출 해야함.
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        if (target instanceof Connection) {
            DatabaseInfo databaseInfo = (DatabaseInfo) getUrl.invoke(target);
            this.setUrl.invoke(result, databaseInfo);
            String sql = (String) args[0];

            ParsingResult parsingResult = trace.recordSqlInfo(sql);
            if (parsingResult != null) {
                this.setSql.invoke(result, parsingResult);
            }

            trace.recordException(result);
        }

        trace.recordApi(descriptor);

        trace.markAfterTime();
        trace.traceBlockEnd();
    }


    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        traceContext.cacheApi(descriptor);
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}
