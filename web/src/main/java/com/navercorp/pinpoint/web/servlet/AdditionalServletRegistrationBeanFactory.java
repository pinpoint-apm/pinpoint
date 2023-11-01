/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.web.servlet;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class AdditionalServletRegistrationBeanFactory {

    private final DispatcherServlet dispatcherServlet;
    private final WebMvcProperties webMvcProperties;
    private final ObjectProvider<MultipartConfigElement> multipartConfig;

    public AdditionalServletRegistrationBeanFactory(
            DispatcherServlet dispatcherServlet,
            WebMvcProperties webMvcProperties,
            ObjectProvider<MultipartConfigElement> multipartConfig
    ) {
        this.dispatcherServlet = Objects.requireNonNull(dispatcherServlet, "dispatcherServlet");
        this.webMvcProperties = Objects.requireNonNull(webMvcProperties, "webMvcProperties");
        this.multipartConfig = Objects.requireNonNull(multipartConfig, "multipartConfig");
    }

    public ServletRegistrationBean<DispatcherServlet> addRegistration(String urlMapping) {
        final ServletRegistrationBean<DispatcherServlet> registrationBean =
                new ServletRegistrationBean<>(dispatcherServlet, urlMapping);
        registrationBean.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
        registrationBean.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
        multipartConfig.ifAvailable(registrationBean::setMultipartConfig);
        return registrationBean;
    }

}
