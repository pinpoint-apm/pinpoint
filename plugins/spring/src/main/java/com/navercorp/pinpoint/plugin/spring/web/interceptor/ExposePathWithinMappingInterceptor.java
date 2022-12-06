package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.web.SpringWebMvcConstants;

import javax.servlet.ServletRequest;
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
        Trace trace = traceContext.currentRawTraceObject();
        if (trace != null) {
            String url = ArrayArgumentUtils.getArgument(args, 0, String.class);
            trace.recordUriTemplate(url);
        }
    }
}
