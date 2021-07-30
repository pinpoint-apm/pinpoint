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

package com.navercorp.pinpoint.collector.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndViewDefiningException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Taejin Koo
 */
public class AuthInterceptor implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String password;
    private final boolean isActive;

    public AuthInterceptor(@Value("${collector.admin.api.rest.active:false}") boolean isActive,
                           @Value("${collector.admin.password:}") String password) {
        this.isActive = isActive;
        this.password = password;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!isActive) {
            throwAuthException("not activating rest api for admin.");
        }

        if (StringUtils.isEmpty(password)) {
            throwAuthException("not activating rest api for admin.");
        }

        String password = request.getParameter("password");
        if (!this.password.equals(password)) {
            throwAuthException("not matched admin password.");
        }

        return true;
    }

    private void throwAuthException(String message) throws ModelAndViewDefiningException {
        logger.warn(message);
        throw new ModelAndViewDefiningException(ControllerUtils.createJsonView(false, message));
    }

}
