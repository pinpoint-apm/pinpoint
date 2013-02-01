package com.profiler.modifier.db.interceptor;

import com.profiler.common.util.ParsingResult;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.*;
import com.profiler.logging.LoggingUtils;
import com.profiler.modifier.db.util.DatabaseInfo;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Logger;

public class PreparedStatementCreateInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport {

    private final Logger logger = Logger.getLogger(PreparedStatementCreateInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    private MethodDescriptor descriptor;

    // connection 용.
    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");
    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);

    private final MetaObject setSql = new MetaObject("__setSql", Object.class);
    private int apiId;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
        }
        if (JDBCScope.isInternal()) {
            logger.fine("internal jdbc scope. skip trace");
            return;
        }
        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        trace.markBeforeTime();

        DatabaseInfo databaseInfo = (DatabaseInfo) getUrl.invoke(target);
        trace.recordRpcName(databaseInfo.getType(), databaseInfo.getDatabaseId(), databaseInfo.getUrl());
        trace.recordEndPoint(databaseInfo.getUrl());

    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
        }
        if (JDBCScope.isInternal()) {
            logger.fine("internal jdbc scope. skip trace");
            return;
        }
        if (!InterceptorUtils.isSuccess(result)) {
            return;
            // TODO traceBlockEnd() 호출 해야함.
        }
        TraceContext traceContext = TraceContext.getTraceContext();
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
//        trace.recordApi(apiId);

        trace.markAfterTime();
        trace.traceBlockEnd();
    }


    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        TraceContext traceContext = TraceContext.getTraceContext();
        traceContext.cacheApi(descriptor);
    }

}
