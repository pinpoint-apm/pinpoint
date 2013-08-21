package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import java.sql.Connection;
import com.nhn.pinpoint.profiler.logging.Logger;

import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.StaticAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.interceptor.util.JDBCScope;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.util.MetaObject;

public class TransactionInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(TransactionInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");
    private MethodDescriptor descriptor;
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
        if (target instanceof Connection) {
            Connection con = (Connection) target;
            if ("setAutoCommit".equals(methodName)) {
                beforeStartTransaction(trace, con);
            } else if ("commit".equals(methodName)) {
                beforeCommit(trace, con);
            } else if ("rollback".equals(methodName)) {
                beforeRollback(trace, con);
            }
        }
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, className, methodName, parameterDescription, args, result);
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
            if ("setAutoCommit".equals(methodName)) {
                afterStartTransaction(trace, con, args, result);
            } else if ("commit".equals(methodName)) {
                afterCommit(trace, con, result);
            } else if ("rollback".equals(methodName)) {
                afterRollback(trace, con, result);
            }
        }
    }

    private void beforeStartTransaction(Trace trace, Connection target) {
        trace.traceBlockBegin();
        trace.markBeforeTime();

        DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);

        trace.recordServiceType(databaseInfo.getType());


        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());
        trace.recordDestinationAddress(databaseInfo.getHost());
    }

    private void afterStartTransaction(Trace trace, Connection target, Object[] arg, Object result) {
        try {
            trace.recordApi(descriptor, arg);
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


    private void beforeRollback(Trace trace, Connection target) {
        trace.traceBlockBegin();
        trace.markBeforeTime();

        DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);

        trace.recordServiceType(databaseInfo.getType());

        trace.recordEndPoint(databaseInfo.getMultipleHost());
        trace.recordDestinationId(databaseInfo.getDatabaseId());
        trace.recordDestinationAddress(databaseInfo.getHost());
    }

    private void afterRollback(Trace trace, Connection target, Object result) {
        try {

            DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);

            trace.recordServiceType(databaseInfo.getType());

            trace.recordEndPoint(databaseInfo.getMultipleHost());
            trace.recordDestinationId(databaseInfo.getDatabaseId());
            trace.recordDestinationAddress(databaseInfo.getHost());

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
