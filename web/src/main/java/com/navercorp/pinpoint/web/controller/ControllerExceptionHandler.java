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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Taejin Koo
 */
@ControllerAdvice
public class ControllerExceptionHandler {

    private static final String DEFAULT_ERROR_VIEW = "jsonView";

    private static final String UNKNOWN = "UNKNOWN";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @ExceptionHandler(value = Exception.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest request, Exception exception) throws Exception {
        Map<String, Object> requestResource = createRequestResource(request);
        logger.warn("Failed to execute controller methods. message:{}, request:{}.", getExceptionMessage(exception), requestResource, exception);

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", createExceptionResource(request, exception));
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
    }
    
    @ExceptionHandler(value = AccessDeniedException.class)
    public ModelAndView accessDeniedExceptionHandler(HttpServletRequest request, Exception exception) throws Exception {
        throw exception;
    }

    private Map<String, Object> createExceptionResource(HttpServletRequest request, Throwable throwable) {
        Map<String, Object> exceptionMap = new HashMap<>();

        exceptionMap.put("message", getExceptionMessage(throwable));
        exceptionMap.put("stacktrace", getExceptionStackTrace(throwable));
        exceptionMap.put("request", createRequestResource(request));

        return exceptionMap;
    }

    private String getExceptionMessage(Throwable throwable) {
        if (throwable == null) {
            return UNKNOWN;
        }

        return throwable.getMessage();
    }

    private String getExceptionStackTrace(Throwable throwable) {
        if (throwable == null) {
            return UNKNOWN;
        }
        
        StringBuilder stackTrace = new StringBuilder(128);
        stackTrace.append(throwable.toString());
        stackTrace.append("\r\n");
        
        for (StackTraceElement traceElement : throwable.getStackTrace()) {
            stackTrace.append("\tat " + traceElement.toString());
            stackTrace.append("\r\n");
        }

        return stackTrace.toString();
    }

    private Map<String, Object> createRequestResource(HttpServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> requestMap = new HashMap<>();

        requestMap.put("method", request.getMethod());
        requestMap.put("url", getRequestUrl(request));
        requestMap.put("heads", getRequestHeaders(request));
        requestMap.put("parameters", getRequestParameters(request));

        return requestMap;
    }
    
    private String getRequestUrl(HttpServletRequest request) {
        if (request.getRequestURL() == null) {
            return UNKNOWN;
        }

        return request.getRequestURL().toString();
    }

    private Map<String, List<String>> getRequestHeaders(HttpServletRequest request) {
        Enumeration keys = request.getHeaderNames();
        if (keys == null) {
            return Collections.emptyMap();
        }
        
        Map<String, List<String>> result = new HashMap<>();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key == null) {
                continue;
            }

            result.put(key.toString(), getRequestHeaderValueList(request, key.toString()));
        }

        return result;
    }

    private List<String> getRequestHeaderValueList(HttpServletRequest request, String key) {
        Enumeration headerValues = request.getHeaders(key);
        if (headerValues == null) {
            return Collections.emptyList();
        }
        
        List<String> headerValueList = new ArrayList<>();
        while (headerValues.hasMoreElements()) {
            Object headerValue = headerValues.nextElement();
            if (headerValue == null) {
                headerValueList.add("null");
            } else {
                headerValueList.add(headerValue.toString());
            }
        }
        
        return headerValueList;
    }

    private Map<String, List<String>> getRequestParameters(HttpServletRequest request) {
        Enumeration keys = request.getParameterNames();
        if (keys == null) {
            return Collections.emptyMap();
        }
        
        Map<String, List<String>> result = new HashMap<>();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key == null) {
                continue;
            }

            result.put(key.toString(), getRequestParameterValueList(request, key.toString()));
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
