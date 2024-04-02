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

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.inspector.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStat;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;

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
    private final String agentStatTopic;
    private final String applicationStatTopic;
    private final TenantProvider tenantProvider;

    public DefaultAgentStatDao(Function<AgentStatBo, List<T>> dataPointFunction, KafkaTemplate<String, AgentStat> kafkaAgentStatTemplate, KafkaTemplate<String, ApplicationStat> kafkaApplicationStatTemplate, BiFunction<List<T>, String, List<AgentStat>> convertToKafkaAgentStatModelFunction, Function<List<AgentStat>, List<ApplicationStat>> convertToKafkaApplicationStatModelFunction, String agentStatTopic, String applicationStatTopic, TenantProvider tenantProvider) {
        this.dataPointFunction = Objects.requireNonNull(dataPointFunction, "dataPointFunction");
        this.kafkaAgentStatTemplate = Objects.requireNonNull(kafkaAgentStatTemplate, "kafkaAgentStatTemplate");
        this.kafkaApplicationStatTemplate = Objects.requireNonNull(kafkaApplicationStatTemplate, "kafkaApplicationStatTemplate");
        this.convertToKafkaAgentStatModelFunction = Objects.requireNonNull(convertToKafkaAgentStatModelFunction, "convertToKafkaAgentStatModelFunction");
        this.convertToKafkaApplicationStatModelFunction = Objects.requireNonNull(convertToKafkaApplicationStatModelFunction, "convertToKafkaApplicationStatModelFunction");
        this.agentStatTopic = Objects.requireNonNull(agentStatTopic, "agentStatTopic");
        this.applicationStatTopic = Objects.requireNonNull(applicationStatTopic, "applicationStatTopic");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    @Override
    public void insert(String agentId, List<T> agentStatData) {
        List<AgentStat> agentStatList = convertToKafkaAgentStatModel(agentStatData);
        for (AgentStat agentStat : agentStatList) {
            kafkaAgentStatTemplate.send(agentStatTopic, agentStat.getSortKey(), agentStat);
        }

        List<ApplicationStat> applicationStatList = convertToKafkaApplicationStatModel(agentStatList);
        for (ApplicationStat applicationStat : applicationStatList) {
            kafkaApplicationStatTemplate.send(applicationStatTopic, applicationStat.getSortKey(), applicationStat);
        }

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
