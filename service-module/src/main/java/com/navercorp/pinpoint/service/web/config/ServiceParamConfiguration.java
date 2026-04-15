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

package com.navercorp.pinpoint.service.web.config;

import com.navercorp.pinpoint.service.web.resolver.CookieServiceNameExtractor;
import com.navercorp.pinpoint.service.web.resolver.DefaultServiceNameExtractor;
import com.navercorp.pinpoint.service.web.resolver.HeaderServiceNameExtractor;
import com.navercorp.pinpoint.service.web.resolver.QueryParamServiceNameExtractor;
import com.navercorp.pinpoint.service.web.resolver.ServiceNameExtractor;
import com.navercorp.pinpoint.service.web.resolver.ServiceParamArgumentResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Configuration
public class ServiceParamConfiguration implements WebMvcConfigurer {

    private final ObjectProvider<ServiceNameExtractor> serviceNameExtractors;

    public ServiceParamConfiguration(ObjectProvider<ServiceNameExtractor> serviceNameExtractors) {
        this.serviceNameExtractors = serviceNameExtractors;
    }

    @Bean
    @Order(1)
    public ServiceNameExtractor queryParamServiceNameExtractor() {
        return new QueryParamServiceNameExtractor();
    }

    @Bean
    @Order(2)
    public ServiceNameExtractor headerServiceNameExtractor() {
        return new HeaderServiceNameExtractor();
    }

    @Bean
    @Order(3)
    public ServiceNameExtractor cookieServiceNameExtractor() {
        return new CookieServiceNameExtractor();
    }

    @Bean
    @Order(Integer.MAX_VALUE)
    public ServiceNameExtractor defaultServiceNameExtractor() {
        return new DefaultServiceNameExtractor();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        List<ServiceNameExtractor> extractors = new ArrayList<>();

        for (ServiceNameExtractor e : serviceNameExtractors) {
            extractors.add(e);
        }

        AnnotationAwareOrderComparator.sort(extractors);

        resolvers.add(new ServiceParamArgumentResolver(extractors));
    }
}
