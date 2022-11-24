package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.spring.web.SpringWebMvcConstants;

import javax.servlet.http.HttpServletRequest;

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
        String url = (String)((HttpServletRequest)args[1]).getAttribute(SpringWebMvcConstants.SPRING_MVC_DEFAULT_URI_ATTRIBUTE_KEY);
        traceContext.currentTraceObject().setUriTemplate(url);
    }
}
