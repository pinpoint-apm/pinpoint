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

package com.navercorp.pinpoint.exceptiontrace.web;

import com.navercorp.pinpoint.exceptiontrace.web.config.ExceptionTracePinotDaoConfiguration;
import com.navercorp.pinpoint.exceptiontrace.web.config.ExceptionTraceProperties;
import com.navercorp.pinpoint.exceptiontrace.web.frontend.export.ExceptionTracePropertiesExporter;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author intr3p1d
 */
@Configuration
@ComponentScan({
        "com.navercorp.pinpoint.common.server.mapper",
        "com.navercorp.pinpoint.exceptiontrace.web.config",
        "com.navercorp.pinpoint.exceptiontrace.web.controller",
        "com.navercorp.pinpoint.exceptiontrace.web.dao",
        "com.navercorp.pinpoint.exceptiontrace.web.mapper",
        "com.navercorp.pinpoint.exceptiontrace.web.service",
})
@Import({
        ExceptionTraceWebPropertySources.class,
        ExceptionTracePinotDaoConfiguration.class,
        PinotConfiguration.class
})
@ConditionalOnProperty(name = "pinpoint.modules.web.exceptiontrace.enabled", havingValue = "true")
public class ExceptionTraceWebConfig {

    @Bean
    public ExceptionTraceProperties exceptionTraceProperties() {
        return new ExceptionTraceProperties();
    }

    @Bean
    public ExceptionTracePropertiesExporter exceptionTracePropertiesExporter(ExceptionTraceProperties exceptionTraceProperties) {
        return new ExceptionTracePropertiesExporter(exceptionTraceProperties);
    }
}
