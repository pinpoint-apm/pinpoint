package com.profiler.modifier.db.interceptor;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

public class TransactionInterceptor implements StaticAroundInterceptor {

    private final Logger logger = Logger.getLogger(TransactionInterceptor.class.getName());

    private final MetaObject<String> getUrl = new MetaObject<String>("__getUrl");

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
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
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
                afterStartTransaction(trace, con, args[0], result);
            } else if ("commit".equals(methodName)) {
                afterCommit(trace, con, result);
            } else if ("rollback".equals(methodName)) {
                afterRollback(trace, con, result);
            }
        }
    }

    private void beforeStartTransaction(Trace trace, Connection target) {

        trace.traceBlockBegin();
        String connectionUrl = this.getUrl.invoke(target);
        trace.recordRpcName("MYSQL", connectionUrl);
        trace.recordTerminalEndPoint(connectionUrl);
        trace.record(Annotation.ClientSend);
    }

    private void afterStartTransaction(Trace trace, Connection target, Object arg, Object result) {
        try {
            Boolean autocommit = (Boolean) arg;
            boolean success = InterceptorUtils.isSuccess(result);
            if (!autocommit) {
                // transaction start;
                if (success) {
                    trace.recordAttibute("Transaction", "begin");
                } else {
                    trace.recordAttibute("Transaction", "begin fail");
                    Throwable th = (Throwable) result;
                    trace.recordAttibute("Exception", th.getMessage());
                }
                trace.record(Annotation.ClientRecv);
            } else {
                if (success) {
                    trace.recordAttibute("Transaction", "autoCommit:false");
                } else {
                    trace.recordAttibute("Transaction", "autoCommit:false fail");
                    Throwable th = (Throwable) result;
                    trace.recordAttibute("Exception", th.getMessage());
                }
                trace.record(Annotation.ClientRecv);
            }
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private void beforeCommit(Trace trace, Connection target) {
        trace.traceBlockBegin();
        String connectionUrl = this.getUrl.invoke(target);
        trace.recordRpcName("MYSQL", connectionUrl);
        trace.recordTerminalEndPoint(connectionUrl);
        trace.record(Annotation.ClientSend);

    }

    private void afterCommit(Trace trace, Connection target, Object result) {
        try {
            String connectionUrl = this.getUrl.invoke(target);
            trace.recordRpcName("MYSQL", connectionUrl);
            trace.recordTerminalEndPoint(connectionUrl);

            boolean success = InterceptorUtils.isSuccess(result);
            if (success) {
                trace.recordAttibute("Transaction", "commit");
            } else {
                trace.recordAttibute("Transaction", "commit fail");
                Throwable th = (Throwable) result;
                trace.recordAttibute("Exception", th.getMessage());
            }
            trace.record(Annotation.ClientRecv);
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }


    private void beforeRollback(Trace trace, Connection target) {
        trace.traceBlockBegin();
        String connectionUrl = this.getUrl.invoke(target);
        trace.recordRpcName("MYSQL", connectionUrl);
        trace.recordTerminalEndPoint(connectionUrl);
        trace.record(Annotation.ClientSend);
    }

    private void afterRollback(Trace trace, Connection target, Object result) {
        try {

            String connectionUrl = this.getUrl.invoke(target);
            trace.recordRpcName("MYSQL", connectionUrl);
            trace.recordTerminalEndPoint(connectionUrl);

            boolean success = InterceptorUtils.isSuccess(result);
            if (success) {
                trace.recordAttibute("Transaction", "rollback");
            } else {
                trace.recordAttibute("Transaction", "rollback fail");
                Throwable th = (Throwable) result;
                trace.recordAttibute("Exception", th.getMessage());
            }
            trace.record(Annotation.ClientRecv);
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

}
