package com.nhn.pinpoint.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * FIXME temporary interceptor for admin operations.
 * 
 * @author hyungil.jeong
 */
public class AdminAuthInterceptor extends HandlerInterceptorAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("#{pinpointWebProps['admin.password']}")
    private String password;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        String requestIp = request.getRemoteAddr();
        logger.info("{} called from {}", requestUri, requestIp);
        String requestPassword = request.getParameter("password");
        if (password.equals(requestPassword)) {
            return true;
        }
        response.sendRedirect("/");
        return false;
    }

    
}
