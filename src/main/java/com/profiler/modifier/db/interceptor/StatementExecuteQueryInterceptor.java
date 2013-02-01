package com.profiler.modifier.db.interceptor;

import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.modifier.db.util.DatabaseInfo;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author netspider
 */
public class StatementExecuteQueryInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport {

    private final Logger logger = Logger.getLogger(StatementExecuteQueryInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");
    private MethodDescriptor descriptor;

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
        try {
            /**
             * If method was not called by request handler, we skip tagging.
             */
            DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);
            trace.recordRpcName(databaseInfo.getType(), databaseInfo.getDatabaseId(), databaseInfo.getUrl());
            trace.recordEndPoint(databaseInfo.getUrl());


        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }


    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
        }
        if (JDBCScope.isInternal()) {
            return;
        }
        TraceContext traceContext = TraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.recordApi(descriptor);
        trace.recordException(result);
        if (args.length > 0) {
            Object arg = args[0];
            if (arg instanceof String) {
                trace.recordSqlInfo((String) arg);
                // TODO parsing result 추가 처리 고려
            }
        }

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
