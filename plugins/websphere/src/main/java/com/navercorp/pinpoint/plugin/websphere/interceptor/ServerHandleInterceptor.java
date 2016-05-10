package com.navercorp.pinpoint.plugin.websphere.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethod;
import com.ibm.websphere.servlet.request.IRequest;

/**
 * @Author Sachin Mittal
 */
@TargetMethod(name = "handleRequest", paramTypes = { "com.ibm.websphere.servlet.request.IRequest" })
public class ServerHandleInterceptor extends AbstractServerHandleInterceptor {

    public ServerHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, Filter<String> excludeFilter) {
        super(traceContext, descriptor, excludeFilter);
    }

    @Override
    protected IRequest getRequest(Object[] args) {
        final Object iRequestObject = args[0];
        if (!(iRequestObject instanceof com.ibm.websphere.servlet.request.IRequest)) {
           return null;
        }
        return (IRequest) iRequestObject;
    }

}
