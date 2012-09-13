package com.profiler.modifier.db.interceptor;

import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionInterceptor implements StaticAroundInterceptor {

        private final Logger logger = Logger.getLogger(TransactionInterceptor.class.getName());

    private final MetaObject<String> getUrl = new MetaObject<String>("__getUrl");

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
        }
        if (target instanceof Connection) {
            Connection con = (Connection) target;
            if ("setAutoCommit".equals(methodName)) {
                beforeStartTransaction(con);
            } else if ("commit".equals(methodName)) {
                beforeCommit(con);
            } else if ("rollback".equals(methodName)) {
                beforeRollback(con);
            }
        }
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
        }
        if (target instanceof Connection) {
            Connection con = (Connection) target;
            if ("setAutoCommit".equals(methodName)) {
                afterStartTransaction(con, args[0], result);
            } else if ("commit".equals(methodName)) {
                afterCommit(con, result);
            } else if ("rollback".equals(methodName)) {
                afterRollback(con, result);
            }
        }
    }

    private void beforeStartTransaction(Connection target) {

        Trace.traceBlockBegin();
        try {
            String connectionUrl = this.getUrl.invoke(target);
            Trace.recordRpcName("mysql", connectionUrl);
            Trace.record(Annotation.ClientSend);
        } finally {
            Trace.traceBlockEnd();
        }
    }

    private void afterStartTransaction(Connection target, Object arg, Object result) {
        Trace.traceBlockBegin();
        try {
            Boolean autocommit = (Boolean) arg;
            boolean success = InterceptorUtils.isSuccess(result);
            if (!autocommit) {
                // transaction start;
                if (success) {
                    Trace.recordAttibute("Transaction", "begin");
                } else {
                    Trace.recordAttibute("Transaction", "begin fail");
                    Throwable th = (Throwable) result;
                    Trace.recordAttibute("Exception", th.getMessage());
                }
                Trace.record(Annotation.ClientRecv);
            } else {
                if (success) {
                    Trace.recordAttibute("Transaction", "state restore");
                } else {
                    Trace.recordAttibute("Transaction", "state restore fail");
                    Throwable th = (Throwable) result;
                    Trace.recordAttibute("Exception", th.getMessage());
                }
                Trace.record(Annotation.ClientRecv);
            }
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            Trace.traceBlockEnd();
        }
    }

    private void beforeCommit(Connection target) {
        Trace.traceBlockBegin();
        try {
            String connectionUrl = this.getUrl.invoke(target);
            Trace.recordRpcName("mysql", connectionUrl);
            Trace.record(Annotation.ClientSend);
        } finally {
            Trace.traceBlockEnd();
        }
    }

    private void afterCommit(Connection target, Object result) {
        Trace.traceBlockBegin();
        try {
            String connectionUrl = this.getUrl.invoke(target);
            Trace.recordRpcName("mysql", connectionUrl);

            boolean success = InterceptorUtils.isSuccess(result);
            if (success) {
                Trace.recordAttibute("Transaction", "commit");
            } else {
                Trace.recordAttibute("Transaction", "commit fail");
                Throwable th = (Throwable) result;
                Trace.recordAttibute("Exception", th.getMessage());
            }
            Trace.record(Annotation.ClientRecv);
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            Trace.traceBlockEnd();
        }
    }


    private void beforeRollback(Connection target) {
        Trace.traceBlockBegin();
        try {
            String connectionUrl = this.getUrl.invoke(target);
            Trace.recordRpcName("mysql", connectionUrl);
            Trace.record(Annotation.ClientSend);
        } finally {
            Trace.traceBlockEnd();
        }
    }

    private void afterRollback(Connection target, Object result) {
        Trace.traceBlockBegin();
        try {
            // TODO 너무 인터널 레벨로 byte code를 수정하다보니, 드라이버내의 close() 메소드가 rollback을 호출하는 것 까지 보임.
            // ex : mysql
            //java.lang.Exception
            //  at com.profiler.modifier.db.mysql.interceptor.TransactionInterceptor.after(TransactionInterceptor.java:24)
            //	at com.mysql.jdbc.ConnectionImpl.rollback(ConnectionImpl.java:4761) 여기에서 다시 부름.
            //  at com.mysql.jdbc.ConnectionImpl.realClose(ConnectionImpl.java:4345)
            //  at com.mysql.jdbc.ConnectionImpl.close(ConnectionImpl.java:1564)

            String connectionUrl = this.getUrl.invoke(target);
            Trace.recordRpcName("mysql", connectionUrl);

            boolean success = InterceptorUtils.isSuccess(result);
            if (success) {
                Trace.recordAttibute("Transaction", "rollback");
            } else {
                Trace.recordAttibute("Transaction", "rollback fail");
                Throwable th = (Throwable) result;
                Trace.recordAttibute("Exception", th.getMessage());
            }
            Trace.record(Annotation.ClientRecv);
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            Trace.traceBlockEnd();
        }
    }

}
