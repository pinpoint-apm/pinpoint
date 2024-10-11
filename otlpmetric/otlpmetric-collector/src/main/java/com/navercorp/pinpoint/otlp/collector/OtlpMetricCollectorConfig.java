/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.collector;

import com.navercorp.pinpoint.otlp.collector.config.OtlpMetricPropertySources;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        PinotConfiguration.class,
        OtlpMetricPropertySources.class})
@ComponentScan({
        "com.navercorp.pinpoint.otlp.collector.config",
        "com.navercorp.pinpoint.otlp.collector.controller",
        "com.navercorp.pinpoint.otlp.collector.dao",
        "com.navercorp.pinpoint.otlp.collector.service",
        "com.navercorp.pinpoint.otlp.collector.mapper",
})
@ConditionalOnProperty(name = "pinpoint.modules.collector.otlpmetric.enabled", havingValue = "true")
public class OtlpMetricCollectorConfig {
}
