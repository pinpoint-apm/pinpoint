package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.spring.web.SpringWebMvcConstants;

import javax.servlet.http.HttpServletRequest;

public class ExposePathWithinMappingInterceptor implements AroundInterceptor {
    private TraceContext traceContext;


    public ExposePathWithinMappingInterceptor(final TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (traceContext.currentTraceObject() != null) {
            String url = ((String) args[0]);
            traceContext.currentTraceObject().setUriTemplate(url);
        }
    }
}
