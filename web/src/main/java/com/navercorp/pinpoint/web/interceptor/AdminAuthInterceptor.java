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

import com.navercorp.pinpoint.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * FIXME temporary interceptor for admin operations.
 * 
 * @author hyungil.jeong
 */
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final Logger logger = LogManager.getLogger(this.getClass());
    
    private final String password;

    public AdminAuthInterceptor(String password) {
        this.password = password;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        String requestIp = request.getRemoteAddr();
        logger.info("{} called from {}", requestUri, requestIp);
        if (StringUtils.isEmpty(password)) {
            return true;
        }
        return checkAuthorization(request);
    }

    private boolean checkAuthorization(HttpServletRequest request) {
        String requestPassword = request.getParameter("password");
        if (requestPassword == null) {
            handleMissingPassword();
            return false;
        }
        if (password.equals(requestPassword)) {
            return true;
        } else {
            handleInvalidPassword();
            return false;
        }
    }

    private void handleMissingPassword() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing password");
    }

    private void handleInvalidPassword() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid password");
    }

    
}
