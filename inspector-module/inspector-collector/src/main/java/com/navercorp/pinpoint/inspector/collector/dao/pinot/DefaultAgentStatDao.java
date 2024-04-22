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

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.inspector.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStatV2;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStat;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author minwoo.jung
 */
public class DefaultAgentStatDao <T extends AgentStatDataPoint> implements AgentStatDao<T> {

    private final Logger logger = LogManager.getLogger(DefaultAgentStatDao.class.getName());

    private final Function<AgentStatBo, List<T>> dataPointFunction;
    private final BiFunction<List<T>, String, List<AgentStat>> convertToKafkaAgentStatModelFunction;
    private final Function<List<AgentStat>, List<ApplicationStat>> convertToKafkaApplicationStatModelFunction;
    private final KafkaTemplate<String, AgentStat> kafkaAgentStatTemplate;
    private final KafkaTemplate<byte[], AgentStatV2> kafkaAgentStatV2Template;
    private final KafkaTemplate<String, ApplicationStat> kafkaApplicationStatTemplate;
    private final String agentStatTopic;
    private final String agentStatTopicV2;
    private final String applicationStatTopic;
    private final TenantProvider tenantProvider;
    private final HashFunction hashFunction = Hashing.murmur3_128();

    public DefaultAgentStatDao(Function<AgentStatBo, List<T>> dataPointFunction, KafkaTemplate<String, AgentStat> kafkaAgentStatTemplate, KafkaTemplate<byte[], AgentStatV2> kafkaAgentStatV2Template, KafkaTemplate<String, ApplicationStat> kafkaApplicationStatTemplate, BiFunction<List<T>, String, List<AgentStat>> convertToKafkaAgentStatModelFunction, Function<List<AgentStat>, List<ApplicationStat>> convertToKafkaApplicationStatModelFunction, String agentStatTopic, String applicationStatTopic, TenantProvider tenantProvider) {
        this.dataPointFunction = Objects.requireNonNull(dataPointFunction, "dataPointFunction");
        this.kafkaAgentStatTemplate = Objects.requireNonNull(kafkaAgentStatTemplate, "kafkaAgentStatTemplate");
        this.kafkaAgentStatV2Template = Objects.requireNonNull(kafkaAgentStatV2Template, "kafkaAgentStatTemplate");
        this.kafkaApplicationStatTemplate = Objects.requireNonNull(kafkaApplicationStatTemplate, "kafkaApplicationStatTemplate");
        this.convertToKafkaAgentStatModelFunction = Objects.requireNonNull(convertToKafkaAgentStatModelFunction, "convertToKafkaAgentStatModelFunction");
        this.convertToKafkaApplicationStatModelFunction = Objects.requireNonNull(convertToKafkaApplicationStatModelFunction, "convertToKafkaApplicationStatModelFunction");
        this.agentStatTopic = Objects.requireNonNull(agentStatTopic, "agentStatTopic");
        this.agentStatTopicV2 = Objects.requireNonNull(agentStatTopic + "-v2", "agentStatTopicV2");
        this.applicationStatTopic = Objects.requireNonNull(applicationStatTopic, "applicationStatTopic");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    @Override
    public void insert(String agentId, List<T> agentStatData) {
        List<AgentStat> agentStatList = convertToKafkaAgentStatModel(agentStatData);
        for (AgentStat agentStat : agentStatList) {
            kafkaAgentStatTemplate.send(agentStatTopic, agentStat.getSortKey(), agentStat);
        }

//After completing the performance comparison, temporarily comment out the code.
//        List<AgentStatV2> agentStatV2List = convertToKafkaAgentStatV2Model(agentStatList);
//        byte[] kafkaKey = generateKafkaKey(agentStatV2List);
//        for (AgentStatV2 agentStatV2 : agentStatV2List) {
//            kafkaAgentStatV2Template.send(agentStatTopicV2, kafkaKey, agentStatV2);
//        }

        List<ApplicationStat> applicationStatList = convertToKafkaApplicationStatModel(agentStatList);
        for (ApplicationStat applicationStat : applicationStatList) {
            kafkaApplicationStatTemplate.send(applicationStatTopic, applicationStat.getSortKey(), applicationStat);
        }

    }

    private byte[] generateKafkaKey(List<AgentStatV2> agentStatV2List) {
        if (agentStatV2List.isEmpty()) {
            return new byte[0];
        }

        return agentStatV2List.get(0).getSortKey().toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<AgentStatV2> convertToKafkaAgentStatV2Model(List<AgentStat> agentStatList) {
        if (agentStatList.isEmpty()) {
            return Collections.emptyList();
        }

        List<AgentStatV2> agentStatV2List = new ArrayList<>(agentStatList.size());
        Long sortKey = hashFunction.hashString(agentStatList.get(0).getSortKey(), StandardCharsets.UTF_8).asLong();
        for (AgentStat agentStat : agentStatList) {
            agentStatV2List.add(new AgentStatV2(agentStat, sortKey));
        }

        return agentStatV2List;
    }

    private List<AgentStat> convertToKafkaAgentStatModel(List<T> AgentStatDataPointList) {
        return convertToKafkaAgentStatModelFunction.apply(AgentStatDataPointList, tenantProvider.getTenantId());
    }

    private List<ApplicationStat> convertToKafkaApplicationStatModel(List<AgentStat> agentStatList) {
        return convertToKafkaApplicationStatModelFunction.apply(agentStatList);

    }

    @Override
    public void dispatch(AgentStatBo agentStatBo) {
        Objects.requireNonNull(agentStatBo, "agentStatBo");
        List<T> dataPointList = this.dataPointFunction.apply(agentStatBo);
        insert(agentStatBo.getAgentId(), dataPointList);
    }
}
