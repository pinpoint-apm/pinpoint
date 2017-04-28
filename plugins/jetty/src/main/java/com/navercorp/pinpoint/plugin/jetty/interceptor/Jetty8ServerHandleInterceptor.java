package com.navercorp.pinpoint.plugin.jetty.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.eclipse.jetty.server.Request;

import java.lang.reflect.Method;

/**
 * @author Taejin Koo
 */
public class Jetty8ServerHandleInterceptor extends AbstractServerHandleInterceptor {

    private volatile Method getRequestMethod;

    public Jetty8ServerHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, Filter<String> excludeFilter) {
        super(traceContext, descriptor, excludeFilter);
    }

    @Override
    protected Request getRequest(Object[] args) {
        try {
            final Object object = args[0];


            final Method getRequestMethod = getGetRequestMethod(object);
            if (getRequestMethod != null) {
                final Request request = (Request) getRequestMethod.invoke(object);
                return request;
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return null;
    }

    private Method getGetRequestMethod(Object target) {
        if (getRequestMethod != null) {
            return getRequestMethod;
        }

        synchronized (this) {
            if (getRequestMethod != null) {
                return getRequestMethod;
            }

            try {
                final Class<?> clazz = target.getClass();
                final Method findedMethod = clazz.getMethod("getRequest");
                if (findedMethod != null) {
                    getRequestMethod = findedMethod;
                    return getRequestMethod;
                }
            } catch (NoSuchMethodException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return null;
    }

}
