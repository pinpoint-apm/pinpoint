package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.web.SpringWebMvcConstants;

import javax.servlet.ServletRequest;

public class LookupHandlerMethodInterceptor implements AroundInterceptor {
    private TraceContext traceContext;


    public LookupHandlerMethodInterceptor(final TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Trace trace = traceContext.currentTraceObject();
        if (trace != null) {
            ServletRequest arg = ArrayArgumentUtils.getArgument(args, 1, ServletRequest.class);
            String url = (String) arg.getAttribute(SpringWebMvcConstants.SPRING_MVC_DEFAULT_URI_ATTRIBUTE_KEY);
            trace.recordUriTemplate(url);
        }
    }
}
