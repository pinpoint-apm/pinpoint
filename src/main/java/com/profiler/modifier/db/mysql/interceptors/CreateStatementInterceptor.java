package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.modifier.db.ConnectionTrace;
import com.profiler.util.ReflectionUtils;

import java.beans.Statement;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateStatementInterceptor implements StaticAfterInterceptor {

    private final Logger logger = Logger.getLogger(CreateStatementInterceptor.class.getName());

    private Field urlField;

    @Override
    public void after(Object target, String className, String methodName, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after className:" + className + " methodName:" + methodName + " args:" + Arrays.toString(args) + " result:" + result);
        }
        if (Trace.getCurrentTraceId() == null) {
            return;
        }
            if (target instanceof Connection) {
                ConnectionTrace connectionTrace = ConnectionTrace.getConnectionTrace();
                String connectionUrl = connectionTrace.getConnectionUrl((Connection) target);

                try {
                    Method setUrl = result.getClass().getMethod("__setUrl", String.class);
                    setUrl.invoke(result, connectionUrl);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InvocationTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
//                Method[] declaredMethods = result.getClass().getDeclaredMethods();
//                for(Method m : declaredMethods) {
//                    System.out.println(m);
//                }
//                Field urlField = getURLField(result);
//                urlField.setAccessible(true);
//                urlField.set(result, connectionUrl);
            }


    }

    private Field getURLField(Object result) {
        Field urlField = this.urlField;
        if(urlField == null) {
            urlField  = ReflectionUtils.findField(result.getClass(), "__url");
            this.urlField = urlField;
        }
        return urlField;
    }

    private Field findField(Object result, String fieldName) {
        Field[] declaredFields = result.getClass().getDeclaredFields();
        for(Field f: declaredFields) {
            if(f.getName().equals(fieldName)) {
                return f;
            }
        }
        return null;
    }
}
