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

import com.navercorp.pinpoint.common.dao.pinot.AgentStatTopicNameManager;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.inspector.collector.config.InspectorCollectorProperties;
import com.navercorp.pinpoint.inspector.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStat;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
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
    private final KafkaTemplate<String, ApplicationStat> kafkaApplicationStatTemplate;
    private final int agentStatTopicCount;
    private final String applicationStatTopicName;
    private final TenantProvider tenantProvider;
    private final AgentStatTopicNameManager agentStatTopicNameManager;

    public DefaultAgentStatDao(Function<AgentStatBo,
                               List<T>> dataPointFunction,
                               KafkaTemplate<String, AgentStat> kafkaAgentStatTemplate,
                               KafkaTemplate<String, ApplicationStat> kafkaApplicationStatTemplate,
                               BiFunction<List<T>, String, List<AgentStat>> convertToKafkaAgentStatModelFunction,
                               Function<List<AgentStat>, List<ApplicationStat>> convertToKafkaApplicationStatModelFunction,
                               InspectorCollectorProperties inspectorCollectorProperties,
                               TenantProvider tenantProvider) {
        this.dataPointFunction = Objects.requireNonNull(dataPointFunction, "dataPointFunction");
        this.kafkaAgentStatTemplate = Objects.requireNonNull(kafkaAgentStatTemplate, "kafkaAgentStatTemplate");
        this.kafkaApplicationStatTemplate = Objects.requireNonNull(kafkaApplicationStatTemplate, "kafkaApplicationStatTemplate");
        this.convertToKafkaAgentStatModelFunction = Objects.requireNonNull(convertToKafkaAgentStatModelFunction, "convertToKafkaAgentStatModelFunction");
        this.convertToKafkaApplicationStatModelFunction = Objects.requireNonNull(convertToKafkaApplicationStatModelFunction, "convertToKafkaApplicationStatModelFunction");
        Objects.requireNonNull(inspectorCollectorProperties, "inspectorCollectorProperties");
        this.agentStatTopicCount = inspectorCollectorProperties.getAgentStatTopicCount();
        this.applicationStatTopicName = inspectorCollectorProperties.getApplicationStatTopicName();
        this.agentStatTopicNameManager = new AgentStatTopicNameManager(inspectorCollectorProperties.getAgentStatTopicPrefix(), inspectorCollectorProperties.getAgentStatTopicPaddingLength());
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    @Override
    public void insert(String applicationName, String agentId, List<T> agentStatData) {
        if (!validateTime(agentStatData)) {
            return;
        };

        List<AgentStat> agentStatList = convertToKafkaAgentStatModel(agentStatData);
        String topicName = agentStatTopicNameManager.getAgentStatTopicName(applicationName, agentStatTopicCount);

        for (AgentStat agentStat : agentStatList) {
            kafkaAgentStatTemplate.send(topicName, agentStat.getSortKey(), agentStat);
        }

        List<ApplicationStat> applicationStatList = convertToKafkaApplicationStatModel(agentStatList);
        for (ApplicationStat applicationStat : applicationStatList) {
            kafkaApplicationStatTemplate.send(applicationStatTopicName, applicationStat.getSortKey(), applicationStat);
        }

    }

    private boolean validateTime(List<T> agentStatData) {
        if (agentStatData.isEmpty()) {
            return false;
        }

        T agentStat = agentStatData.get(0);
        Instant collectedTime = Instant.ofEpochMilli(agentStat.getTimestamp());
        Instant validTime = Instant.now().minus(Duration.ofMinutes(10));

        if (validTime.isBefore(collectedTime)) {
            return true;
        }

        logger.info("AgentStat data is invalid. applicationName: {}, agentId: {}, time: {}", agentStat.getApplicationName(), agentStat.getAgentId(), new Date(agentStat.getTimestamp()));
        return false;
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
        insert(agentStatBo.getApplicationName(), agentStatBo.getAgentId(), dataPointList);
    }
}
