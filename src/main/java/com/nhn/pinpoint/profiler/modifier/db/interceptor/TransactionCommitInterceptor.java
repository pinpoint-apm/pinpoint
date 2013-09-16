package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.profiler.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.*;
import com.nhn.pinpoint.profiler.interceptor.util.JDBCScope;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.util.MetaObject;

import java.sql.Connection;

public class TransactionCommitInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(TransactionCommitInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");
    private MethodDescriptor descriptor;
    private TraceContext traceContext;

    public TransactionCommitInterceptor() {
    }

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
        if (target instanceof Connection) {
            Connection con = (Connection) target;
            beforeCommit(trace, con);
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
        if (target instanceof Connection) {
            Connection con = (Connection) target;
            afterCommit(trace, con, result);
        }
    }


    private void beforeCommit(Trace trace, Connection target) {
        trace.traceBlockBegin();
        trace.markBeforeTime();

        DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);

        trace.recordServiceType(databaseInfo.getType());

        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());
        trace.recordDestinationAddress(databaseInfo.getHost());

    }

    private void afterCommit(Trace trace, Connection target, Object result) {
        try {
            DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);

            trace.recordServiceType(databaseInfo.getType());

            trace.recordEndPoint(databaseInfo.getMultipleHost());
            trace.recordDestinationId(databaseInfo.getDatabaseId());
            trace.recordDestinationAddress(databaseInfo.getHost());

            trace.recordApi(descriptor);
            trace.recordException(result);

            trace.markAfterTime();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
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
