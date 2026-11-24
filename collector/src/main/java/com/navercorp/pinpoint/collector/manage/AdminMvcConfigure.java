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

package com.navercorp.pinpoint.collector.manage;

import com.navercorp.pinpoint.collector.manage.controller.AuthInterceptor;
import com.navercorp.pinpoint.common.server.config.ActuatorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.MappedInterceptor;


/**
 * Guards the admin API and the actuator endpoints with the admin password. Only the health check stays open.
 */
@Configuration
public class AdminMvcConfigure {

    private static final String ADMIN_PATH = "/admin/**";

    @Bean
    public ActuatorProperties actuatorProperties() {
        return new ActuatorProperties();
    }

    /**
     * A {@link MappedInterceptor} bean is picked up by every handler mapping. An interceptor of a
     * {@code WebMvcConfigurer} is not: the handler mapping of the actuator endpoints ignores it.
     */
    @Bean
    public MappedInterceptor adminAuthInterceptor(AuthInterceptor interceptor, ActuatorProperties actuatorProperties) {
        String basePath = actuatorProperties.getBasePath();
        String[] includePatterns = {ADMIN_PATH, basePath, basePath + "/**"};

        String healthPath = actuatorProperties.getHealthPath();
        String[] excludePatterns = {healthPath, healthPath + "/**"};
        return new MappedInterceptor(includePatterns, excludePatterns, interceptor);
    }

}
