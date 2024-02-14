/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.collector;

import com.navercorp.pinpoint.inspector.collector.config.InspectorKafkaConfiguration;
import com.navercorp.pinpoint.inspector.collector.config.InspectorPropertySources;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * @author minwoo.jung
 */
@ComponentScan({"com.navercorp.pinpoint.inspector.collector"})
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@Import({
            InspectorPropertySources.class,
            InspectorKafkaConfiguration.class})
@ConditionalOnProperty(name = "pinpoint.modules.collector.inspector.enabled", havingValue = "true")
public class InspectorCollectorConfig {
}
