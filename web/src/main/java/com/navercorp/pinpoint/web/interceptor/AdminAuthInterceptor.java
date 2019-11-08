/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * FIXME temporary interceptor for admin operations.
 * 
 * @author hyungil.jeong
 */
public class AdminAuthInterceptor extends HandlerInterceptorAdapter {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("${admin.password:}")
    private String password;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        String requestIp = request.getRemoteAddr();
        logger.info("{} called from {}", requestUri, requestIp);
        if (StringUtils.isEmpty(password)) {
            return true;
        }
        return checkAuthorization(request, response);
    }

    private boolean checkAuthorization(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestPassword = request.getParameter("password");
        if (requestPassword == null) {
            handleMissingPassword(response);
            return false;
        }
        if (password.equals(requestPassword)) {
            return true;
        } else {
            handleInvalidPassword(response);
            return false;
        }
    }

    private void handleMissingPassword(HttpServletResponse response) throws IOException {
        ServletServerHttpResponse serverResponse = new ServletServerHttpResponse(response);
        serverResponse.setStatusCode(HttpStatus.BAD_REQUEST);
        serverResponse.getBody().write("Missing password.".getBytes(UTF_8));
    }

    private void handleInvalidPassword(HttpServletResponse response) throws IOException {
        ServletServerHttpResponse serverResponse = new ServletServerHttpResponse(response);
        serverResponse.setStatusCode(HttpStatus.FORBIDDEN);
        serverResponse.getBody().write("Invalid password.".getBytes(UTF_8));
    }

    
}
