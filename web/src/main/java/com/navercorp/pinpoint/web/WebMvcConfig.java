/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.web.interceptor.AdminAuthInterceptor;
import com.navercorp.pinpoint.web.vo.tree.SortByRequestConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String[] RESOURCE_LOCATION = {"classpath:/static/"};

    private static final String[] LONG_TIME_AVAILABLE_RESOURCE_TYPE = {"/**/**.ico", "/**/**.png", "/**/**.gif", "/**/**.jpg", "/**/**.woff", "/**/**.woff2"};

    private static final String[] SHORT_TIME_AVAILABLE_RESOURCE_TYPE = {"/**/**.js", "/**/**.css", "/**/**.html"};


    @Value("${pinpoint.web.cache-resources:false}")
    private boolean cacheResource;

    @Value("${admin.password:}")
    private String password;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // For using like WelcomePageHandler
        registry.addViewController("/").setViewName("forward:/index.html");
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor(password))
                .addPathPatterns("/admin/**");
    }

    public ResourceHandlerConfigure newResourceHandlerConfigure() {
        return new ResourceHandlerConfigure(cacheResource);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        final ResourceHandlerConfigure configure = newResourceHandlerConfigure();

        // index.html no-cache
        configure.apply(registry.addResourceHandler("/index.html")
                .addResourceLocations(RESOURCE_LOCATION)
                .setCacheControl(CacheControl.noCache())
        );

        // Resources that don't change well : 1 day
        configure.apply(registry.addResourceHandler(LONG_TIME_AVAILABLE_RESOURCE_TYPE)
                .addResourceLocations(RESOURCE_LOCATION)
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
        );

        // Resources that change well : 2 minutes
        configure.apply(registry.addResourceHandler(SHORT_TIME_AVAILABLE_RESOURCE_TYPE)
                .addResourceLocations(RESOURCE_LOCATION)
                .setCacheControl(CacheControl.maxAge(2, TimeUnit.MINUTES).cachePublic())
        );

        // default resource handler
        configure.apply(registry.addResourceHandler("/**")
                .addResourceLocations(RESOURCE_LOCATION)
                .setCacheControl(CacheControl.noCache())
        );
    }

    static class ResourceHandlerConfigure {

        private final boolean cacheResources;
        private final ResourceResolver encodedResourceResolve = new EncodedResourceResolver();

        public ResourceHandlerConfigure(boolean cacheResources) {
            this.cacheResources = cacheResources;
        }

        public void apply(ResourceHandlerRegistration registration) {
            registration.resourceChain(cacheResources)
                    .addResolver(encodedResourceResolve);
        }

    }


    @Override
    public void addFormatters(FormatterRegistry registry) {
        WebMvcConfigurer.super.addFormatters(registry);
        registry.addConverter(new SortByRequestConverter());
    }
}
