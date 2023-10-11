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

import com.navercorp.pinpoint.common.server.bo.stat.ProfilerMetricBo;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ProfilerMetricDao {
    private final Logger logger = LogManager.getLogger(DefaultAgentStatDao.class.getName());
    private final Function<ProfilerMetricBo, List<AgentStat>> convertToKafkaModelFunction;
    private final KafkaTemplate kafkaAgentStatTemplate;
    private final String topic;

    public ProfilerMetricDao(KafkaTemplate kafkaAgentStatTemplate, Function<ProfilerMetricBo, List<AgentStat>> convertToKafkaModelFunction, String topic) {
        this.kafkaAgentStatTemplate = Objects.requireNonNull(kafkaAgentStatTemplate, "kafkaAgentStatTemplate");
        this.convertToKafkaModelFunction = convertToKafkaModelFunction;
        this.topic = topic;
    }

    public void dispatch(ProfilerMetricBo profilerMetricBo) {
        Objects.requireNonNull(profilerMetricBo, "profilerMetricBo");
        insert(profilerMetricBo.getAgentId(), profilerMetricBo);
    }

    private void insert(String agentId, ProfilerMetricBo profilerMetricBo) {
        List<AgentStat> agentStatList = convertToKafkaModelFunction.apply(profilerMetricBo);

        for (AgentStat agentStat : agentStatList) {
            String kafkaKey = generateKafkaKey(agentStat);
            kafkaAgentStatTemplate.send(topic, kafkaKey, agentStat);
        }
    }

    private String generateKafkaKey(AgentStat agentStat) {
        StringBuilder sb = new StringBuilder();
        sb.append(agentStat.getApplicationName());
        sb.append("_");
        sb.append(agentStat.getAgentId());
        sb.append("_");
        sb.append(agentStat.getMetricName());
        return sb.toString();
    }
}
