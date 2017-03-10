package com.navercorp.pinpoint.plugin.axis2.jaxws.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethod;
import org.apache.axis2.jaxws.core.MessageContext;

/**
 * @Author Sachin Mittal
 */
@TargetMethod(name = "handleRequest", paramTypes = { "org.apache.axis2.jaxws.core.MessageContext" })
public class MessageHandleInterceptor extends AbstractMessageHandleInterceptor {

    public MessageHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, Filter<String> excludeFilter) {
        super(traceContext, descriptor, excludeFilter);
    }

    @Override
    protected MessageContext getRequest(Object[] args) {
        final Object iRequestObject = args[0];
        if (!(iRequestObject instanceof org.apache.axis2.jaxws.core.MessageContext)) {
           return null;
        }
        return (MessageContext) iRequestObject;
    }

}
