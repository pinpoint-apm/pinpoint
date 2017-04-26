package com.navercorp.pinpoint.plugin.jetty.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;

/**
 * @author Taejin Koo
 */
public class ServerHandleInterceptor extends AbstractServerHandleInterceptor {

    public ServerHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, Filter<String> excludeFilter) {
        super(traceContext, descriptor, excludeFilter);
    }

    @Override
    protected Request getRequest(Object[] args) {
        final Object httpChannelObject = args[0];
        if (!(httpChannelObject instanceof HttpChannel)) {
           return null;
        }
        final HttpChannel<?> channel = (HttpChannel<?>) httpChannelObject;
        final Request request = channel.getRequest();
        return request;
    }

}
