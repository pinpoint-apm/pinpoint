/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web;

import com.navercorp.pinpoint.common.server.metric.dao.TableNameManager;
import com.navercorp.pinpoint.metric.web.config.SystemMetricProperties;
import com.navercorp.pinpoint.metric.web.frontend.export.SystemMetricPropertiesExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author minwoo-jung
 */
@Configuration
public class MetricWebConfig {

    @Bean
    public SystemMetricProperties systemMetricProperties() {
        return new SystemMetricProperties();
    }

    @Bean
    public SystemMetricPropertiesExporter systemMetricPropertiesExporter(SystemMetricProperties systemMetricProperties) {
        return new SystemMetricPropertiesExporter(systemMetricProperties);
    }

    @Bean
    TableNameManager systemMetricDoubleTableNameManager(SystemMetricProperties properties) {
        if ("single".equalsIgnoreCase(properties.getSystemMetricDoubleTableMode())) {
            return new TableNameManager(properties.getSystemMetricDoubleSingleTableName());
        }
        return new TableNameManager(properties.getSystemMetricTablePrefix(), properties.getSystemMetricTablePaddingLength(), properties.getSystemMetricTableCount());
    }
}
