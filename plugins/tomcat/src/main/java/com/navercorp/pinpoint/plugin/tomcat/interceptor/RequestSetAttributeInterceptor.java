package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import org.apache.catalina.connector.Request;

public class RequestSetAttributeInterceptor implements AroundInterceptor {
    private final String bestMatchingPatternKey = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";
    private final String bestMatchingPatternBeforeError = RequestSetAttributeInterceptor.class.getName() + ".bestMatchingPattern";
    @Override
    public void before(Object target, Object[] args) {
        Request request = (Request) target;
        Object error = request.getAttribute("javax.servlet.error.exception");

        if (error != null) {
            String attributeName = (String) args[0];
            if (attributeName.equals(bestMatchingPatternKey)) {
                request.setAttribute(bestMatchingPatternBeforeError, request.getAttribute(bestMatchingPatternKey));
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
