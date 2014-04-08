package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.util.MetaObject;

import java.sql.Connection;

/**
 * @author emeroad
 */
public class TransactionRollbackInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetaObject<DatabaseInfo> getDatabaseInfo = new MetaObject<DatabaseInfo>(UnKnownDatabaseInfo.INSTANCE, "__getDatabaseInfo");
    private MethodDescriptor descriptor;
    private TraceContext traceContext;

    public TransactionRollbackInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        if (target instanceof Connection) {
            Connection con = (Connection) target;
            beforeRollback(trace, con);
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
        if (target instanceof Connection) {
            Connection con = (Connection) target;
            afterRollback(trace, con, result);
        }
    }



    private void beforeRollback(Trace trace, Connection target) {
        trace.traceBlockBegin();
        trace.markBeforeTime();

    }

    private void afterRollback(Trace trace, Connection target, Object result) {
        try {

            DatabaseInfo databaseInfo = this.getDatabaseInfo.invoke(target);
            if (databaseInfo == null) {
                databaseInfo = UnKnownDatabaseInfo.INSTANCE;
            }
            trace.recordServiceType(databaseInfo.getType());
            trace.recordEndPoint(databaseInfo.getMultipleHost());
            trace.recordDestinationId(databaseInfo.getDatabaseId());


            trace.recordApi(descriptor);
            trace.recordException(result);
//            boolean success = InterceptorUtils.isSuccess(result);
//            if (success) {
//                trace.recordAttribute("Transaction", "rollback");
//            } else {
//                trace.recordAttribute("Transaction", "rollback fail");
//            }
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
