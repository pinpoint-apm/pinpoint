package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.modifier.db.ConnectionTrace;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionInterceptor implements StaticAroundInterceptor {

    private final Logger logger = Logger.getLogger(TransactionInterceptor.class.getName());

     private final MetaObject<String> getUrl = new MetaObject<String>("_getUrl", String.class);

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
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

        if ("setAutoCommit".equals(methodName)) {
            startTransaction(target, args[0], result);
        } else if ("commit".equals(methodName)) {
            commit(target, result);
        } else if ("rollback".equals(methodName)) {
            rollback(target, result);
        }
    }



    private void startTransaction(Object target, Object arg, Object result) {
        Trace.traceBlockBegin();
        try {
            String connectionUrl = this.getUrl.invoke((Connection) target);
            Trace.recordRpcName("mysql", connectionUrl);
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
                Trace.record(Annotation.ClientSend);
                Trace.record(Annotation.ClientRecv);
            } else {
                if (success) {
                    Trace.recordAttibute("Transaction", "state restore");
                } else {
                    Trace.recordAttibute("Transaction", "state restore fail");
                    Throwable th = (Throwable) result;
                    Trace.recordAttibute("Exception", th.getMessage());
                }
                Trace.record(Annotation.ClientSend);
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

    private void commit(Object target, Object result) {
        Trace.traceBlockBegin();
        try {
            String connectionUrl = this.getUrl.invoke((Connection) target);
            Trace.recordRpcName("mysql", connectionUrl);

            boolean success = InterceptorUtils.isSuccess(result);
            if (success) {
                Trace.recordAttibute("Transaction", "commit");
            } else {
                Trace.recordAttibute("Transaction", "commit fail");
                Throwable th = (Throwable) result;
                Trace.recordAttibute("Exception", th.getMessage());
            }
            Trace.record(Annotation.ClientSend);
            Trace.record(Annotation.ClientRecv);
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } finally {
            Trace.traceBlockEnd();
        }
    }

    private void rollback(Object target, Object result) {
        Trace.traceBlockBegin();
        try {
            // TODO 너무 인터널 레벨로 byte code를 수정하다보니, 드라이버내의 close() 메소드가 rollback을 호출하는 것 까지 보임.
            // ex : mysql
            //java.lang.Exception
	        //  at com.profiler.modifier.db.mysql.interceptors.TransactionInterceptor.after(TransactionInterceptor.java:24)
	        //	at com.mysql.jdbc.ConnectionImpl.rollback(ConnectionImpl.java:4761) 여기에서 다시 부름.
		    //  at com.mysql.jdbc.ConnectionImpl.realClose(ConnectionImpl.java:4345)
		    //  at com.mysql.jdbc.ConnectionImpl.close(ConnectionImpl.java:1564)

            String connectionUrl = this.getUrl.invoke((Connection) target);
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
