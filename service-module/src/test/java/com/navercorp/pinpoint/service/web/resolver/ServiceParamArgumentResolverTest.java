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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author minwoo.jung
 */
@ExtendWith(MockitoExtension.class)
class ServiceParamArgumentResolverTest {

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    private ServiceParamArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        List<ServiceNameExtractor> extractors = List.of(new DefaultServiceNameExtractor());
        resolver = new ServiceParamArgumentResolver(extractors);
    }

    @Test
    void supportsParameter_WithServiceParamAnnotationAndServiceInfoType_ReturnsTrue() {
        when(methodParameter.hasParameterAnnotation(ServiceParam.class)).thenReturn(true);
        when(methodParameter.getParameterType()).thenReturn((Class) ServiceName.class);

        boolean result = resolver.supportsParameter(methodParameter);

        assertThat(result).isTrue();
    }

    @Test
    void supportsParameter_WithoutServiceParamAnnotation_ReturnsFalse() {
        when(methodParameter.hasParameterAnnotation(ServiceParam.class)).thenReturn(false);

        boolean result = resolver.supportsParameter(methodParameter);

        assertThat(result).isFalse();
    }

    @Test
    void supportsParameter_WithDifferentParameterType_ReturnsFalse() {
        when(methodParameter.hasParameterAnnotation(ServiceParam.class)).thenReturn(true);
        when(methodParameter.getParameterType()).thenReturn((Class) String.class);

        boolean result = resolver.supportsParameter(methodParameter);

        assertThat(result).isFalse();
    }

    @Test
    void resolveArgument_WithDefaultExtractor_ReturnsDefault() {
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);

        Object result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isInstanceOf(ServiceName.class);
        ServiceName serviceName = (ServiceName) result;
        assertThat(serviceName.getName()).isEqualTo("DEFAULT");
    }

    @Test
    void resolveArgument_WithNoServiceName_ReturnsNull() {
        List<ServiceNameExtractor> emptyExtractors = Collections.emptyList();
        ServiceParamArgumentResolver emptyResolver = new ServiceParamArgumentResolver(emptyExtractors);

        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);

        Object result = emptyResolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isNull();
    }

    @Test
    void resolveArgument_WithNullHttpServletRequest_ReturnsNull() {
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(null);

        Object result = resolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isNull();
    }

    @Test
    void resolveArgument_WithBlankServiceName_SkipsAndContinues() {
        ServiceNameExtractor blankExtractor = mock(ServiceNameExtractor.class);
        ServiceNameExtractor validExtractor = mock(ServiceNameExtractor.class);

        when(blankExtractor.extract(httpServletRequest)).thenReturn("");
        when(validExtractor.extract(httpServletRequest)).thenReturn("validService");

        List<ServiceNameExtractor> extractors = Arrays.asList(blankExtractor, validExtractor);
        ServiceParamArgumentResolver customResolver = new ServiceParamArgumentResolver(extractors);

        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);

        Object result = customResolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isInstanceOf(ServiceName.class);
        ServiceName serviceName = (ServiceName) result;
        assertThat(serviceName.getName()).isEqualTo("validService");
    }

    @Test
    void resolveArgument_WithMultipleExtractors_UsesFirstValidOne() {
        ServiceNameExtractor nullExtractor = mock(ServiceNameExtractor.class);
        ServiceNameExtractor validExtractor = mock(ServiceNameExtractor.class);

        when(nullExtractor.extract(httpServletRequest)).thenReturn(null);
        when(validExtractor.extract(httpServletRequest)).thenReturn("firstValid");

        List<ServiceNameExtractor> extractors = Arrays.asList(nullExtractor, validExtractor);
        ServiceParamArgumentResolver customResolver = new ServiceParamArgumentResolver(extractors);

        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);

        Object result = customResolver.resolveArgument(methodParameter, null, webRequest, null);

        assertThat(result).isInstanceOf(ServiceName.class);
        ServiceName serviceName = (ServiceName) result;
        assertThat(serviceName.getName()).isEqualTo("firstValid");
    }
}
