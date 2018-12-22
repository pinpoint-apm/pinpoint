package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.transaction.IMappingRegistry;
import com.navercorp.pinpoint.bootstrap.context.transaction.IRequestMappingInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.transaction.RequestMappingInfo;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashSet;
import java.util.Set;

public class RegisterInterceptor implements AroundInterceptor {

    private final TraceContext traceContext;

    public RegisterInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        IMappingRegistry registry = traceContext.getMappingRegistry();

        if(args[0] instanceof org.springframework.web.servlet.mvc.method.RequestMappingInfo){
            registry.register(createRequestMappingInfo((org.springframework.web.servlet.mvc.method.RequestMappingInfo)args[0]), 1);
        }
        System.out.println(registry);
        System.out.println(target.getClass());
    }

    private IRequestMappingInfo createRequestMappingInfo(org.springframework.web.servlet.mvc.method.RequestMappingInfo springRequestMappingInfo) {
        Set<String> patterns = springRequestMappingInfo.getPatternsCondition().getPatterns();
        Set<RequestMethod> methods = springRequestMappingInfo.getMethodsCondition().getMethods();

        if(patterns != null && patterns.size() > 0 && methods!= null && methods.size() > 0){
            return new RequestMappingInfo(patterns.iterator().next(), convert(methods));
        }
        return null;
    }

    private Set<String> convert(Set<RequestMethod> methods){
        Set<String> result = new HashSet<String>();
        for(RequestMethod requestMethod : methods){
            result.add(requestMethod.name());
        }

        return result;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }
}
