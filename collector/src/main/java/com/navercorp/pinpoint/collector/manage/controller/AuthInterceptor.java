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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.response.MapResponse;
import com.navercorp.pinpoint.common.server.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Objects;


/**
 * @author Taejin Koo
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper;

    private final String password;
    private final boolean isActive;

    public AuthInterceptor(ObjectMapper mapper,
                           @Value("${collector.admin.api.rest.active:false}") boolean isActive,
                           @Value("${collector.admin.password:}") String password) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.isActive = isActive;
        this.password = password;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!isActive) {
            String jsonError = jsonError("not activating rest api for admin");
            writeJsonError(response, HttpStatus.UNAUTHORIZED, jsonError);
            return false;
        }

        if (StringUtils.isEmpty(password)) {
            String jsonError = jsonError("not activating rest api for admin");
            writeJsonError(response, HttpStatus.UNAUTHORIZED, jsonError);
            return false;
        }

        String password = request.getParameter("password");
        if (!this.password.equals(password)) {
            String jsonError = jsonError("not matched admin password");
            writeJsonError(response, HttpStatus.FORBIDDEN, jsonError);
            return false;
        }

        return true;
    }

    private String jsonError(String errorMessage) throws JsonProcessingException {
        MapResponse response = new MapResponse(Result.FAIL, errorMessage);
        return mapper.writeValueAsString(response);
    }

    private void writeJsonError(HttpServletResponse response, HttpStatus unauthorized, String jsonError) throws IOException {
        logger.warn("Authorization Error: {}", jsonError);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(unauthorized.value());
        response.getWriter().write(jsonError);
    }


    @Override
    public String toString() {
        return "AuthInterceptor{" +
                "password='" + password + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
