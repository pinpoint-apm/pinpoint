package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.util.MetaObject;

/**
 * @author netspider
 * @author emeroad
 */
public class StatementExecuteQueryInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetaObject<DatabaseInfo> getDatabaseInfo = new MetaObject<DatabaseInfo>(UnKnownDatabaseInfo.INSTANCE, "__getDatabaseInfo");
    private MethodDescriptor descriptor;
    private TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
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
            DatabaseInfo databaseInfo = this.getDatabaseInfo.invoke(target);
            if (databaseInfo == null) {
                databaseInfo = UnKnownDatabaseInfo.INSTANCE;
            }
            trace.recordServiceType(databaseInfo.getExecuteQueryType());
            trace.recordEndPoint(databaseInfo.getMultipleHost());
            trace.recordDestinationId(databaseInfo.getDatabaseId());

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
