package com.profiler.modifier.db.interceptor;

import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.modifier.db.util.DatabaseInfo;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreparedStatementCreateInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport {
    private final Logger logger = Logger.getLogger(PreparedStatementCreateInterceptor.class.getName());

    private MethodDescriptor descriptor;

    // connection 용.
    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");
    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);

    private final MetaObject setSql = new MetaObject("__setSql", String.class);

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        if (JDBCScope.isInternal()) {
            logger.info("internal jdbc scope. skip trace");
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
        trace.recordTerminalEndPoint(databaseInfo.getUrl());

    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        if (JDBCScope.isInternal()) {
            logger.info("internal jdbc scope. skip trace");
            return;
        }
        if (!InterceptorUtils.isSuccess(result)) {
            return;
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
            this.setSql.invoke(result, sql);

            trace.recordException(result);
//            trace.recordAttribute("PreparedStatement", sql);
        }

        trace.recordApi(descriptor, args);

        trace.markAfterTime();
        trace.traceBlockEnd();
    }


    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
    }
}
