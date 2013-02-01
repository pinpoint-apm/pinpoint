package com.profiler.modifier.db.interceptor;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.modifier.db.util.DatabaseInfo;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

public class TransactionInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport {

    private final Logger logger = Logger.getLogger(TransactionInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");
    private MethodDescriptor descriptor;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            logger.fine("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
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
            logger.fine("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        if (JDBCScope.isInternal()) {
            return;
        }
        TraceContext traceContext = TraceContext.getTraceContext();
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
        trace.recordRpcName(databaseInfo.getType(), databaseInfo.getDatabaseId(), databaseInfo.getUrl());
        trace.recordEndPoint(databaseInfo.getUrl());
    }

    private void afterStartTransaction(Trace trace, Connection target, Object[] arg, Object result) {
        try {
            trace.recordApi(descriptor, arg);
//            trace.recordApi(apiId, arg);
            trace.recordException(result);
//            Boolean autocommit = (Boolean) arg;
//            boolean success = InterceptorUtils.isSuccess(result);
//            if (!autocommit) {
//                // transaction start;
//                if (success) {
//                    trace.recordAttribute("Transaction", "begin");
//                    trace.recordApi(descriptor, null);
//                } else {
//                    trace.recordAttribute("Transaction", "begin fail");
//                    Throwable th = (Throwable) result;
//                    trace.recordAttribute("Exception", th.getMessage());
//                }
//
//            } else {
//                if (success) {
//                    trace.recordAttribute("Transaction", "autoCommit:false");
//                } else {
//                    trace.recordAttribute("Transaction", "autoCommit:false fail");
//                    Throwable th = (Throwable) result;
//                    trace.recordAttribute("Exception", th.getMessage());
//                }
//
//            }
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            trace.markAfterTime();
            trace.traceBlockEnd();
        }
    }

    private void beforeCommit(Trace trace, Connection target) {
        trace.traceBlockBegin();
        trace.markBeforeTime();

        DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);
        trace.recordRpcName(databaseInfo.getType(), databaseInfo.getDatabaseId(), databaseInfo.getUrl());
        trace.recordEndPoint(databaseInfo.getUrl());
//        trace.record(Annotation.ClientSend);

    }

    private void afterCommit(Trace trace, Connection target, Object result) {
        try {
            DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);
            trace.recordRpcName(databaseInfo.getType(), databaseInfo.getDatabaseId(), databaseInfo.getUrl());
            trace.recordEndPoint(databaseInfo.getUrl());

            trace.recordApi(descriptor);
//            trace.recordApi(apiId);
            trace.recordException(result);

//            boolean success = InterceptorUtils.isSuccess(result);
//            if (success) {
//                trace.recordAttribute("Transaction", "commit");
//            } else {
//                trace.recordAttribute("Transaction", "commit fail");
//                Throwable th = (Throwable) result;
//                trace.recordAttribute("Exception", th.getMessage());
//            }

        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            trace.markAfterTime();
            trace.traceBlockEnd();
        }
    }


    private void beforeRollback(Trace trace, Connection target) {
        trace.traceBlockBegin();
        trace.markBeforeTime();

        DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);
        trace.recordRpcName(databaseInfo.getType(), databaseInfo.getDatabaseId(), databaseInfo.getUrl());
        trace.recordEndPoint(databaseInfo.getUrl());
    }

    private void afterRollback(Trace trace, Connection target, Object result) {
        try {

            DatabaseInfo databaseInfo = (DatabaseInfo) this.getUrl.invoke(target);
            trace.recordRpcName(databaseInfo.getType(), databaseInfo.getDatabaseId(), databaseInfo.getUrl());
            trace.recordEndPoint(databaseInfo.getUrl());

            trace.recordApi(descriptor);
//            trace.recordApi(apiId);
            trace.recordException(result);
//            boolean success = InterceptorUtils.isSuccess(result);
//            if (success) {
//                trace.recordAttribute("Transaction", "rollback");
//            } else {
//                trace.recordAttribute("Transaction", "rollback fail");
//            }
            trace.recordException(result);
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            trace.markAfterTime();
            trace.traceBlockEnd();
        }
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        TraceContext traceContext = TraceContext.getTraceContext();
        traceContext.cacheApi(descriptor);
    }


}
