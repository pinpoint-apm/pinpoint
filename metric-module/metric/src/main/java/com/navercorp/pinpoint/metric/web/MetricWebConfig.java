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

import com.navercorp.pinpoint.common.server.config.YamlConfiguration;
import com.navercorp.pinpoint.common.server.metric.dao.TableNameManager;
import com.navercorp.pinpoint.metric.web.config.MetricWebPinotDaoConfiguration;
import com.navercorp.pinpoint.metric.web.config.SystemMetricProperties;
import com.navercorp.pinpoint.metric.web.frontend.export.SystemMetricPropertiesExporter;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author minwoo-jung
 */
@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.metric.web",
        "com.navercorp.pinpoint.common.server.util"
})
@Import({
        WebMetricPropertySources.class,
        MetricWebPinotDaoConfiguration.class,
        PinotConfiguration.class,
        YamlConfiguration.class
})
@ConditionalOnProperty(value = "pinpoint.modules.web.systemmetric.enabled", havingValue = "true")
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
