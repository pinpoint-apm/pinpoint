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

package com.navercorp.pinpoint.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.response.Response;
import com.navercorp.pinpoint.web.view.error.ExceptionResponse;
import com.navercorp.pinpoint.web.view.error.InternalServerError;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author Taejin Koo
 */
@ControllerAdvice
public class ControllerExceptionHandler {
    private static final String UNKNOWN = "UNKNOWN";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConfigProperties configProperties;

    public ControllerExceptionHandler(ConfigProperties configProperties) {
        this.configProperties = Objects.requireNonNull(configProperties, "configProperties");
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Response> defaultErrorHandler(HttpServletRequest request, Exception exception) throws Exception {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(exception, "exception");

        InternalServerError.RequestInfo requestInfo = createRequestResource(request);
        logger.warn("Failed to execute controller methods. message:{}, request:{}.", exception.getMessage(), requestInfo, exception);
        InternalServerError error = createExceptionResource(requestInfo, exception);

        return ResponseEntity.internalServerError()
                .body(new ExceptionResponse(error));
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<Response> accessDeniedExceptionHandler(HttpServletRequest request, Exception exception) throws Exception {
        throw exception;
    }

    private InternalServerError createExceptionResource(InternalServerError.RequestInfo requestInfo, Throwable throwable) {
        String exceptionMessage = throwable.getMessage();
        String stackTrace = null;
        if (configProperties.isShowStackTraceOnError()) {
            stackTrace = getExceptionStackTrace(throwable);
        }

        return new InternalServerError(exceptionMessage, stackTrace, requestInfo);
    }


    private String getExceptionStackTrace(Throwable throwable) {
        if (throwable == null) {
            return UNKNOWN;
        }
        
        StringBuilder stackTrace = new StringBuilder(128);
        stackTrace.append(throwable);
        stackTrace.append("\r\n");
        
        for (StackTraceElement traceElement : throwable.getStackTrace()) {
            stackTrace.append("\tat ");
            stackTrace.append(traceElement.toString());
            stackTrace.append("\r\n");
        }

        return stackTrace.toString();
    }

    private InternalServerError.RequestInfo createRequestResource(HttpServletRequest request) {

        String method = request.getMethod();
        String url = getRequestUrl(request);
        Map<String, List<String>> headers = getRequestHeaders(request);
        Map<String, List<String>> parameters = getRequestParameters(request);
        return new InternalServerError.RequestInfo(method, url, headers, parameters);
    }
    
    private String getRequestUrl(HttpServletRequest request) {
        if (request.getRequestURL() == null) {
            return UNKNOWN;
        }
        return request.getRequestURL().toString();
    }

    private Map<String, List<String>> getRequestHeaders(HttpServletRequest request) {
        Enumeration<String> keys = request.getHeaderNames();
        if (keys == null) {
            return Collections.emptyMap();
        }
        
        Map<String, List<String>> result = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key == null) {
                continue;
            }

            result.put(key, getRequestHeaderValueList(request, key));
        }

        return result;
    }

    private List<String> getRequestHeaderValueList(HttpServletRequest request, String key) {
        Enumeration<String> headerValues = request.getHeaders(key);
        if (headerValues == null) {
            return Collections.emptyList();
        }

        List<String> headerValueList = new ArrayList<>();
        while (headerValues.hasMoreElements()) {
            String headerValue = headerValues.nextElement();
            if (headerValue == null) {
                headerValueList.add("null");
            } else {
                headerValueList.add(headerValue);
            }
        }
        
        return headerValueList;
    }

    private Map<String, List<String>> getRequestParameters(HttpServletRequest request) {
        Enumeration<String> keys = request.getParameterNames();
        if (keys == null) {
            return Collections.emptyMap();
        }

        Map<String, List<String>> result = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key == null) {
                continue;
            }

            result.put(key, getRequestParameterValueList(request, key));
        }

        return result;
    }

    private List<String> getRequestParameterValueList(HttpServletRequest request, String key) {
        String[] values = request.getParameterValues(key);
        if (values == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(values);
    }

}
