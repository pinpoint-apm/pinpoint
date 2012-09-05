package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.modifier.db.ConnectionTrace;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateStatementInterceptor implements StaticAfterInterceptor {

    private final Logger logger = Logger.getLogger(CreateStatementInterceptor.class.getName());

    private Method setUrl = null;

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
        }
        if (target instanceof Connection) {
            ConnectionTrace connectionTrace = ConnectionTrace.getConnectionTrace();
            String connectionUrl = connectionTrace.getConnectionUrl((Connection) target);
            setUrl(result, connectionUrl);
        }


    }

    private void setUrl(Object result, String connectionUrl) {
        try {
            if (setUrl == null) {
                setUrl = result.getClass().getMethod("__setUrl", String.class);
            }
            setUrl.invoke(result, connectionUrl);
        } catch (NoSuchMethodException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } catch (InvocationTargetException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } catch (IllegalAccessException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

}
