package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.interceptor.util.JDBCScope;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.util.MetaObject;

import com.nhn.pinpoint.profiler.logging.Logger;

/**
 * @author netspider
 */
public class StatementExecuteQueryInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(StatementExecuteQueryInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");
    private MethodDescriptor descriptor;
    private TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
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
        try {
            /**
             * If method was not called by request handler, we skip tagging.
             */
            DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);

            trace.recordServiceType(databaseInfo.getExecuteQueryType());

            trace.recordEndPoint(databaseInfo.getMultipleHost());
            trace.recordDestinationId(databaseInfo.getDatabaseId());
            trace.recordDestinationAddress(databaseInfo.getHost());


        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
    }


    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }
        if (JDBCScope.isInternal()) {
            return;
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
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
        } finally {
            trace.traceBlockEnd();
        }
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
