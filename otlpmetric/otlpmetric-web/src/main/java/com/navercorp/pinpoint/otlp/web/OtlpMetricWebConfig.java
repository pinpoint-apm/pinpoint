package com.navercorp.pinpoint.otlp.web;/*
 * Copyright 2024 NAVER Corp.
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

import com.navercorp.pinpoint.otlp.web.config.OtlpMetricProperties;
import com.navercorp.pinpoint.otlp.web.config.OtlpMetricPropertySources;
import com.navercorp.pinpoint.otlp.web.config.mysql.OtlpMetricWebMysqlDaoConfiguration;
import com.navercorp.pinpoint.otlp.web.config.pinot.OtlpMetricWebPinotDaoConfiguration;
import com.navercorp.pinpoint.otlp.web.config.pinot.OtlpMetricPinotTableProperties;
import com.navercorp.pinpoint.otlp.web.frontend.export.OtlpMetricPropertiesExporter;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan({
        "com.navercorp.pinpoint.otlp.web.controller",
        "com.navercorp.pinpoint.otlp.web.service",
        "com.navercorp.pinpoint.otlp.web.dao"
})
@Import({
        PinotConfiguration.class,
        OtlpMetricWebPinotDaoConfiguration.class,
        OtlpMetricWebMysqlDaoConfiguration.class,
        OtlpMetricPinotTableProperties.class,
        OtlpMetricPropertySources.class
})
@ConditionalOnProperty(name = "pinpoint.modules.web.otlpmetric.enabled", havingValue = "true")
public class OtlpMetricWebConfig {

    @Bean
    public OtlpMetricProperties otlpMetricProperties() {
        return new OtlpMetricProperties();
    }

    @Bean
    public OtlpMetricPropertiesExporter otlpMetricPropertiesExporter(OtlpMetricProperties otlpMetricProperties) {
        return new OtlpMetricPropertiesExporter(otlpMetricProperties);
    }
}
