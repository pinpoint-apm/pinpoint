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

import com.navercorp.pinpoint.common.server.metric.dao.TopicNameManager;
import com.navercorp.pinpoint.inspector.collector.config.InspectorCollectorProperties;
import com.navercorp.pinpoint.inspector.collector.config.InspectorKafkaConfiguration;
import com.navercorp.pinpoint.inspector.collector.config.InspectorPropertySources;
import com.navercorp.pinpoint.inspector.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.collector.dao.pinot.DefaultAgentStatDao;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStat;
import com.navercorp.pinpoint.inspector.collector.service.PinotMappers;
import com.navercorp.pinpoint.pinot.config.PinotConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author minwoo.jung
 */
@ComponentScan({"com.navercorp.pinpoint.inspector.collector"})
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@Import({
        PinotConfiguration.class,
        InspectorPropertySources.class,
        InspectorKafkaConfiguration.class})
@ConditionalOnProperty(name = "pinpoint.modules.collector.inspector.enabled", havingValue = "true")
public class InspectorCollectorConfig {

    @Bean
    public AgentStatDao agentStatDao(KafkaTemplate<String, AgentStat> kafkaAgentStatTemplate,
                                     KafkaTemplate<String, ApplicationStat> kafkaApplicationStatTemplate,
                                     InspectorCollectorProperties properties,
                                     @Qualifier("agentStatDaoTopicNameManager")
                                     TopicNameManager topicNameManager) {
        String topicName = properties.getApplicationStatTopicName();
        return new DefaultAgentStatDao(
                kafkaAgentStatTemplate,
                kafkaApplicationStatTemplate,
                topicName,
                topicNameManager);
    }

    @Bean
    public PinotMappers getPinotStatMappers() {
        return new PinotMappers();
    }

    @Bean(name = "agentStatDaoTopicNameManager")
    public TopicNameManager getTopicNameManager(InspectorCollectorProperties properties) {
        return new TopicNameManager(properties.getAgentStatTopicPrefix(), properties.getAgentStatTopicPaddingLength(), properties.getAgentStatTopicCount());
    }
}
