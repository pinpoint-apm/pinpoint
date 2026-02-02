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

package com.navercorp.pinpoint.otlp.collector.config;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * @author minwoo-jung
 */
@PropertySources({
        @PropertySource(name = "OtlpMetricPropertySources-KAFKA", value = { OtlpMetricPropertySources.KAFKA_TOPIC, OtlpMetricPropertySources.COLLECTOR_CONFIG}),
})
public class OtlpMetricPropertySources {

    public static final String KAFKA_TOPIC = "classpath:otlpmetric/collector/kafka-topic-otlpmetric.properties";

    public static final String COLLECTOR_CONFIG = "classpath:otlpmetric/collector/profiles/${pinpoint.profiles.active:release}/otlpmetric-collector.properties";
}
