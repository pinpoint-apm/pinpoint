package com.navercorp.pinpoint.plugin.jetty.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethod;
import org.eclipse.jetty.server.Request;

import java.lang.reflect.Method;

/**
 * @Author Taejin Koo
 */
@TargetMethod(name = "handle", paramTypes = { "org.eclipse.jetty.server.AbstractHttpConnection" })
public class Jetty8ServerHandleInterceptor extends AbstractServerHandleInterceptor {

    public Jetty8ServerHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, Filter<String> excludeFilter) {
        super(traceContext, descriptor, excludeFilter);
    }

    @Override
    protected Request getRequest(Object[] args) {
        try {
            Object object = args[0];

            Method getRequestMethod = getMethod(object.getClass(), "getRequest");
            Request request = (Request) getRequestMethod.invoke(object);
            return request;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return null;
    }

    private Method getMethod(Class clazz, String methodName) {
        Class targetClazz = clazz;

        while (targetClazz != null) {
            try {
                Method method = targetClazz.getMethod(methodName);
                if (method != null) {
                    return method;
                }

            } catch (NoSuchMethodException e) {
                Class superclass = targetClazz.getSuperclass();
                if (superclass != null) {
                    targetClazz = superclass;
                }
            }
        }

        return null;
    };

}
