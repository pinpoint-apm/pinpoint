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

package com.navercorp.pinpoint.inspector.collector.dao.pinot;

import com.navercorp.pinpoint.common.server.metric.dao.TopicNameManager;
import com.navercorp.pinpoint.inspector.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class DefaultAgentStatDao implements AgentStatDao {

    private final KafkaTemplate<String, AgentStat> kafkaAgentStatTemplate;
    private final KafkaTemplate<String, ApplicationStat> kafkaApplicationStatTemplate;
    private final String applicationStatTopicName;
    private final TopicNameManager topicNameManager;


    public DefaultAgentStatDao(KafkaTemplate<String, AgentStat> kafkaAgentStatTemplate,
                               KafkaTemplate<String, ApplicationStat> kafkaApplicationStatTemplate,
                               String applicationStatTopicName,
                               @Qualifier("agentStatDaoTopicNameManager")
                               TopicNameManager topicNameManager) {
        this.kafkaAgentStatTemplate = Objects.requireNonNull(kafkaAgentStatTemplate, "kafkaAgentStatTemplate");
        this.kafkaApplicationStatTemplate = Objects.requireNonNull(kafkaApplicationStatTemplate, "kafkaApplicationStatTemplate");
        this.applicationStatTopicName = Objects.requireNonNull(applicationStatTopicName, "applicationStatTopicName");
        this.topicNameManager = Objects.requireNonNull(topicNameManager, "topicNameManager");
    }

    @Override
    public void insertAgentStat(List<AgentStat> agentStatList) {
        for (AgentStat agentStat : agentStatList) {
            String topicName = topicNameManager.getTopicName(agentStat.getApplicationName());
            kafkaAgentStatTemplate.send(topicName, agentStat.getSortKey(), agentStat);
        }
    }

    @Override
    public void insertApplicationStat(List<ApplicationStat> applicationStatList) {
        for (ApplicationStat applicationStat : applicationStatList) {
            kafkaApplicationStatTemplate.send(applicationStatTopicName, applicationStat.getSortKey(), applicationStat);
        }
    }

}
