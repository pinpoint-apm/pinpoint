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

package com.navercorp.pinpoint.collector.heatmap;

import com.navercorp.pinpoint.collector.heatmap.config.HeatmapKafkaConfiguration;
import com.navercorp.pinpoint.collector.heatmap.config.HeatmapPropertySources;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * @author minwoo-jung
 */
@ComponentScan({"com.navercorp.pinpoint.collector.heatmap"})
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@Import({
        HeatmapPropertySources.class,
        HeatmapKafkaConfiguration.class})
@ConditionalOnProperty(name = "pinpoint.modules.collector.heatmap.enabled", havingValue = "true")
public class HeatmapCollectorModule {
}
