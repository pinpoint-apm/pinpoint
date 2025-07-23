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
package com.navercorp.pinpoint.agentstatistics.collector.dao;

import com.navercorp.pinpoint.agentstatistics.collector.entity.AgentInfoEntity;
import com.navercorp.pinpoint.agentstatistics.collector.mapper.AgentInfoMapper;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.pinot.kafka.util.KafkaCallbacks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @author intr3p1d
 */
@Repository
public class PinotAgentInfoStatisticsDao implements AgentInfoStatisticsDao {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebugEnabled = logger.isDebugEnabled();

    private final KafkaTemplate<String, AgentInfoEntity> kafkaAgentInfoTemplate;

    private final AgentInfoMapper mapper;

    private final String topic;

    private final BiConsumer<SendResult<String, AgentInfoEntity>, Throwable> resultCallback
            = KafkaCallbacks.loggingCallback("Kafka(AgentInfoEntity)", logger);


    public PinotAgentInfoStatisticsDao(
            @Qualifier("kafkaAgentInfoTemplate") KafkaTemplate<String, AgentInfoEntity> kafkaAgentInfoTemplate,
            @Value("${kafka.agentstatistics.topic}") String topic,
            AgentInfoMapper mapper) {
        this.kafkaAgentInfoTemplate = kafkaAgentInfoTemplate;
        this.mapper = mapper;
        this.topic = topic;
    }

    @Override
    public void insert(
            ServiceUid serviceUid,
            ApplicationUid applicationUid,
            AgentInfoBo agentInfoBo
    ) {
        Objects.requireNonNull(agentInfoBo, "agentInfoBo");

        if (isDebugEnabled) {
            logger.debug("Pinot data insert: {}", agentInfoBo);
        }

        AgentInfoEntity agentInfoEntity = mapper.toEntity(agentInfoBo);
        CompletableFuture<SendResult<String, AgentInfoEntity>> future = kafkaAgentInfoTemplate.send(topic, agentInfoEntity);
        future.whenComplete(resultCallback);
    }
}
