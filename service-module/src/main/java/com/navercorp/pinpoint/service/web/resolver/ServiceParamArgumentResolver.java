/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.service.web.resolver;

import com.navercorp.pinpoint.service.web.vo.ServiceName;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class ServiceParamArgumentResolver implements HandlerMethodArgumentResolver {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final List<ServiceNameExtractor> extractors;

    public ServiceParamArgumentResolver(List<ServiceNameExtractor> extractors) {
        this.extractors = Objects.requireNonNull(extractors, "extractors");
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ServiceParam.class)
                && ServiceName.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            logger.debug("HttpServletRequest is not available, skip ServiceInfo resolving.");
            return null;
        }

        for (ServiceNameExtractor extractor : extractors) {
            String serviceName = extractor.extract(request);
            if (serviceName != null && !serviceName.isBlank()) {
                return new ServiceName(serviceName);
            }
        }

        logger.debug("serviceName not found in any source, skip.");
        return null;
    }
}
