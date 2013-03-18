package com.profiler.modifier.db.interceptor;

import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.modifier.db.util.DatabaseInfo;
import com.profiler.modifier.db.util.JDBCUrlParser;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class DriverConnectInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport {

    private final Logger logger = Logger.getLogger(DriverConnectInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);

    private JDBCUrlParser urlParser = new JDBCUrlParser();

    private MethodDescriptor descriptor;
    private int apiId;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
            logger.fine("JDBCScope push:" + Thread.currentThread().getName());
        }
        JDBCScope.pushScope();

        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
        trace.markBeforeTime();

    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
            logger.fine("JDBCScope pop:" + Thread.currentThread().getName());
        }
        // 여기서는 trace context인지 아닌지 확인하면 안된다. trace 대상 thread가 아닌곳에서 connection이 생성될수 있음.
        JDBCScope.popScope();

        boolean success = InterceptorUtils.isSuccess(result);
        // 여기서는 trace context인지 아닌지 확인하면 안된다. trace 대상 thread가 아닌곳에서 connection이 생성될수 있음.
        DatabaseInfo databaseInfo = createDatabaseInfo((String) args[0]);
        if (success) {
            // 생성이 성공해야 result가 connection임.
            this.setUrl.invoke(result, databaseInfo);
        }

        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.recordServiceType(databaseInfo.getType());

        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());
        trace.recordDestinationAddress(databaseInfo.getHost());



        trace.recordApi(descriptor, new Object[]{args[0]});
        trace.recordException(result);

        trace.markAfterTime();
        trace.traceBlockEnd();
    }

    private DatabaseInfo createDatabaseInfo(String url) {
        DatabaseInfo databaseInfo = urlParser.parse(url);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("parse DatabaseInfo:" + databaseInfo);
        }
        return databaseInfo;
    }


    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        TraceContext traceContext = TraceContext.getTraceContext();
        traceContext.cacheApi(descriptor);
    }


}
