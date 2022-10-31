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

import com.navercorp.pinpoint.web.vo.tree.SortByRequestConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String[] RESOURCE_LOCATION = {"classpath:/resources/", "classpath:/META-INF/resources/", "classpath:/static/", "classpath:/public/", "/"};

    private static final String[] LONG_TIME_AVAILABLE_RESOURCE_TYPE = {"/**/**.ico", "/**/**.png", "/**/**.gif", "/**/**.jpg", "/**/**.woff", "/**/**.woff2"};

    private static final String[] SHORT_TIME_AVAILABLE_RESOURCE_TYPE = {"/**/**.js", "/**/**.css", "/**/**.html"};

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // For using like WelcomePageHandler
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // index.html no-cache
        registry.addResourceHandler("/index.html").addResourceLocations(RESOURCE_LOCATION)
                .setCacheControl(CacheControl.noCache());

        // Resources that don't change well : 1 day
        registry.addResourceHandler(LONG_TIME_AVAILABLE_RESOURCE_TYPE).addResourceLocations(RESOURCE_LOCATION)
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic());

        // Resources that change well : 2 minutes
        registry.addResourceHandler(SHORT_TIME_AVAILABLE_RESOURCE_TYPE).addResourceLocations(RESOURCE_LOCATION)
                .setCacheControl(CacheControl.maxAge(2, TimeUnit.MINUTES).cachePublic());

        // default resource handler
        registry.addResourceHandler("/**").addResourceLocations(RESOURCE_LOCATION)
                .setCacheControl(CacheControl.noCache());
        registry.addResourceHandler("/webjars/**").addResourceLocations(RESOURCE_LOCATION)
                .setCacheControl(CacheControl.noCache());
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        WebMvcConfigurer.super.addFormatters(registry);
        registry.addConverter(new SortByRequestConverter());
    }
}
