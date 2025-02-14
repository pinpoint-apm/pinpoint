/*
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

package com.navercorp.pinpoint.otlp.web.config;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * @author minwoo-jung
 */
@PropertySources({
        @PropertySource(name = "OtlpMetricPropertySources-WEB", value = { OtlpMetricPropertySources.OTLP_METRIC_CONFIG, OtlpMetricPropertySources.OTLP_METRIC_CONFIG_ROOT}),
})
public class OtlpMetricPropertySources {
    public static final String OTLP_METRIC_CONFIG = "classpath:otlpmetric/web/profiles/${pinpoint.profiles.active:release}/otlpmetric-web.properties";
    public static final String OTLP_METRIC_CONFIG_ROOT = "classpath:otlpmetric/web/otlpmetric-web-root.properties";
}
